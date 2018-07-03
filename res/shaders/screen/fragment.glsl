#version 430 core

precision highp float;

const int MAX_MSAA_SAMPLES = 16;
const float epsilon = 0.005;
const float ssaoBias = 0.025;
const vec3 lightDirection = normalize(vec3(0.7, -1.0, 0.3));

in vec3 p_vertexPosition;
in vec2 p_vertexTexture;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform float nearPlane;
uniform float farPlane;
uniform vec3 cameraPosition;
uniform vec3 cameraDirection;
uniform vec4 colourMultiplier = vec4(1.0);
uniform bool wireframe;
uniform bool ambientOcclusion;

uniform int msaaSamples;
uniform int ssaoSamples;
uniform ivec2 screenResolution;
uniform ivec2 ssaoNoiseSize;
uniform float ssaoRadius;
uniform float ssaoTextureScale;
uniform sampler2DMS diffuseTexture;
uniform sampler2DMS normalTexture;
uniform sampler2DMS positionTexture;
uniform sampler2DMS specularTexture;
uniform sampler2DMS depthTexture;
uniform sampler2D ssaoTexture;
uniform sampler2D ssaoNoiseTexture;
uniform sampler1D ssaoSamplesTexture;

out vec4 outColour;

float getDistance(float depth)
{
    return 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));
}

void main(void)
{
    ivec2 texelPosition = ivec2(p_vertexTexture * screenResolution);

    vec4 diffuse = vec4(0.0);
    vec3 normal = vec3(0.0);
    vec3 position = vec3(0.0);
    float specular = 0.0;
    float depth = 0.0;

    float ssao = 0.0;

    if (ambientOcclusion)
    {
        vec2 ssaoSize = vec2(textureSize(ssaoTexture, 0));
        int ssaoBlurRadius = 0;

        for (int i = -ssaoBlurRadius; i <= ssaoBlurRadius; i++)
        {
            for (int j = -ssaoBlurRadius; j <= ssaoBlurRadius; j++)
            {
                ssao += texture2D(ssaoTexture, (p_vertexTexture * ssaoTextureScale) + vec2(i, j) / ssaoSize).r;
            }
        }

        ssao /= ((ssaoBlurRadius * 2 + 1) * (ssaoBlurRadius * 2 + 1));
    } else
    {
        ssao = 1.0;
    }

    for (int i = 0; i < max(1, msaaSamples); i++)
    {
        diffuse += texelFetch(diffuseTexture, texelPosition, i).rgba / max(1, msaaSamples);
        normal += texelFetch(normalTexture, texelPosition, i).xyz / max(1, msaaSamples);
        position += texelFetch(positionTexture, texelPosition, i).xyz / max(1, msaaSamples);
        specular += texelFetch(specularTexture, texelPosition, i).x / max(1, msaaSamples);
        depth += texelFetch(depthTexture, texelPosition, i).r / max(1, msaaSamples);
    }

    normal = (vec4(normalize(normal), 0.0)).xyz;

    float nDotL = clamp(dot(normal, -lightDirection), 0.4, 1.0);

    outColour = vec4(vec3(diffuse * nDotL * ssao), 1.0);//vec4(diffuse.rgb * nDotL, 1.0);
//    outColour = vec4(vec3(position), 1.0);//vec4(diffuse.rgb * nDotL, 1.0);
//    outColour = vec4(vec3(ssao), 1.0);//vec4(diffuse.rgb * nDotL, 1.0);
}
