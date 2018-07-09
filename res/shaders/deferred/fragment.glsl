#version 430 core

precision highp float;

#define MAX_LIGHTS 16

const int MAX_MSAA_SAMPLES = 16;
const float epsilon = 0.05;
const float ssaoBias = 0.025;
const vec3 sunDirection = normalize(vec3(0.7, -1.0, 0.3));
const vec3 sunColour = vec3(0.89, 0.92, 1.0);
const vec3 ambient = vec3(0.005);
const vec3 gamma = vec3(1.0 / 2.2);

struct Light
{
    vec3 position;
    vec3 colour;
    vec3 attenuation;
    float intensity;
    float nearPlane;
    float farPlane;
    samplerCube shadowMap;
};

uniform Light test;

in vec3 p_vertexPosition;
in vec2 p_vertexTexture;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform float nearPlane;
uniform float farPlane;
uniform vec3 cameraPosition;
uniform vec3 cameraDirection;
uniform vec4 colourMultiplier = vec4(1.0);
uniform bool drawWireframe;
uniform bool drawGeometry;
uniform bool ambientOcclusion;

uniform int msaaSamples;
uniform ivec2 screenResolution;
uniform sampler2DMS diffuseTexture;
uniform sampler2DMS normalTexture;
uniform sampler2DMS specularTexture;
uniform sampler2DMS depthTexture;
uniform sampler2D ssaoTexture;
uniform sampler2D shadowTexture;

uniform Light lights[MAX_LIGHTS];

out vec4 outColour;

float getDistance(float depth)
{
    return 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));
}

vec4 getBuffer(sampler2DMS texture, ivec2 texelPosition)
{
    vec4 value = vec4(0.0);

    for (int i = 0; i < max(1, msaaSamples); i++)
        value += texelFetch(texture, texelPosition, i).rgba / max(1, msaaSamples);

    return value;
}

vec3 getLightColour(vec3 surfacePosition, vec3 surfaceNormal, vec4 specular)
{
    vec3 surfaceToCamera = normalize(cameraPosition - surfacePosition);

    vec3 colour = vec3(0.0);

    for (int i = 0; i < MAX_LIGHTS; i++)
    {
        if (lights[i].intensity > 0.0)
        {
            vec3 surfaceToLight = lights[i].position - surfacePosition;
            float dist = length(surfaceToLight);
            surfaceToLight /= dist;

            float nDotL = max(0.0, dot(surfaceNormal, surfaceToLight));

            if (nDotL > 0.0)
            {
                vec3 diffuse = nDotL * lights[i].colour.rgb;

                vec3 specular = specular.rgb * pow(max(0.0, dot(reflect(-surfaceToLight, surfaceNormal), surfaceToCamera)), specular.w) * lights[i].intensity;

                float attenuation = 1.0 / (lights[i].attenuation.x + lights[i].attenuation.y * dist + lights[i].attenuation.z * dist * dist);

                colour += max(vec3(0.0), lights[i].intensity * attenuation * (diffuse + specular));
            }
        }
    }

    return clamp(colour, 0.0, 1.0);
}

vec4 drawLights(float depth)
{
    vec4 colour = vec4(0.0);

    for (int i = 0; i < MAX_LIGHTS; i++)
    {
        vec4 pos = projectionMatrix * viewMatrix * vec4(lights[i].position, 1.0);
        pos.xyz = (pos.xyz / pos.w) * 0.5 + 0.5;

        if (depth > pos.z)
        {
            vec2 d = (p_vertexTexture.xy - pos.xy) * vec2(float(screenResolution.x) / float(screenResolution.y), 1.0);

            float r = 0.04 / (pos.z * pos.w);
            float len = dot(d, d);

            if (len < r * r)
            {
                float f = 1.0;// max(0.0, 1.0 - len / (r * r));

                colour += vec4(vec3(lights[i].colour), f);
            }
        }
    }

    return colour;
}


vec3 getPosition(float depth)
{
  vec4 p_cs = inverse(projectionMatrix) * (vec4(p_vertexTexture.xy, depth, 1.0) * vec4(2.0) - vec4(1.0));
  return (inverse(viewMatrix) * vec4(p_cs.xyz / p_cs.w, 1.0)).xyz;
}

void main(void)
{
    ivec2 texelPosition = ivec2(p_vertexTexture * screenResolution);

    vec4 diffuse = getBuffer(diffuseTexture, texelPosition).rgba;;

    float depth = drawGeometry ? getBuffer(depthTexture, texelPosition).r : 0.0;
    vec3 position = drawGeometry ? getPosition(depth).xyz : vec3(0.0);
    vec3 normal = drawGeometry ? normalize(getBuffer(normalTexture, texelPosition).xyz) : vec3(0.0);
    vec4 specular = drawGeometry ? getBuffer(specularTexture, texelPosition).rgba : vec4(0.0);
    float ssao = drawGeometry && ambientOcclusion ? texture2D(ssaoTexture, p_vertexTexture).r : 1.0;
    float shadowFactor = drawGeometry ? texture2D(shadowTexture, p_vertexTexture).r : 1.0;

    vec4 debugLights = drawLights(depth);
    vec3 lightColour = drawGeometry ? getLightColour(position, normal, specular) : vec3(1.0);

    vec3 finalColour = max(ambient.rgb * diffuse.rgb, diffuse.rgb * lightColour.rgb * shadowFactor) * ssao + debugLights.rgb * debugLights.a;

    outColour = vec4(pow(finalColour, gamma), 1.0);
//    outColour = vec4(vec3(position), 1.0);
}
