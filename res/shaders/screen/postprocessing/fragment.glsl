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
uniform vec4 colourMultiplier = vec4(1.0);
uniform bool wireframe;

uniform int msaaSamples;
uniform int ssaoSamples;
uniform ivec2 screenResolution;
uniform ivec2 ssaoNoiseSize;
uniform float ssaoRadius;
uniform sampler2DMS diffuseTexture;
uniform sampler2DMS normalTexture;
uniform sampler2DMS specularEmission;
uniform sampler2DMS depthTexture;
uniform sampler2D ssaoNoiseTexture;
uniform sampler1D ssaoSamplesTexture;

out vec4 outColour;

float getDistance(float depth)
{
    return 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));
}

vec3 getWorldPosition(vec2 ndc, float depth)
{
    vec4 clipSpacePosition = vec4(ndc * 2.0 - 1.0, depth, 1.0);
    vec4 viewSpacePosition = inverse(projectionMatrix) * clipSpacePosition;

    viewSpacePosition /= viewSpacePosition.w;

    return (inverse(viewMatrix) * viewSpacePosition).xyz;
}

float getAmbientOcclusion(vec3 position, vec3 normal)
{
    vec3 rand = texture2D(ssaoNoiseTexture, p_vertexTexture * (vec2(screenResolution) / vec2(ssaoNoiseSize))).xyz;

    vec3 tangent = normalize(rand - normal * dot(rand, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 normalMatrix = mat3(tangent, bitangent, normal);

    float ambientOcclusion = 0.0;

    for (int i = 0; i < ssaoSamples; i++)
    {
        vec3 samplePoint = position + normalMatrix * texelFetch(ssaoSamplesTexture, i, 0).xyz * ssaoRadius;
        vec4 offset = projectionMatrix * vec4(samplePoint, 1.0);
        offset.xyz = (offset.xyz / offset.w) * 0.5 + 0.5;

        float sampleDepth = texelFetch(depthTexture, ivec2(offset.xy * screenResolution), 0).r;
        ambientOcclusion += (sampleDepth >= samplePoint.z + ssaoBias ? 1.0 : 0.0);
    }

    return ambientOcclusion / ssaoSamples;
}

void main(void)
{
    ivec2 texelPosition = ivec2(p_vertexTexture * screenResolution);

    vec4 diffuse = vec4(0.0);
    vec3 normal = vec3(0.0);
    float specular = 0.0;
    float depth = 0.0;

    for (int i = 0; i < max(1, msaaSamples); i++)
    {
        diffuse += texelFetch(diffuseTexture, texelPosition, i).rgba / max(1, msaaSamples);
        normal += texelFetch(normalTexture, texelPosition, i).rgb / max(1, msaaSamples);
        specular += texelFetch(specularEmission, texelPosition, i).r / max(1, msaaSamples);
        depth += texelFetch(depthTexture, texelPosition, i).r / max(1, msaaSamples);
    }

    vec3 worldPosition = getWorldPosition(p_vertexTexture, depth);

    float nDotL = getAmbientOcclusion(worldPosition, normal);//clamp(dot(normal, -lightDirection), 0.1, 1.0);

    outColour = vec4(worldPosition / 32.0, 1.0);//vec4(diffuse.rgb * nDotL, 1.0);
}
