#version 430 core

precision highp float;

const float ssaoBias = 0.025;

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

uniform ivec2 screenResolution;
uniform sampler2DMS normalTexture;
//uniform sampler2DMS positionTexture;
uniform sampler2DMS depthTexture;

uniform int ssaoSamples;
uniform float ssaoRadius;
uniform float ssaoOffset;
uniform float ssaoTextureScale;
uniform int ssaoNoiseSize;
uniform sampler2D ssaoNoiseTexture;
uniform sampler1D ssaoSamplesTexture;

out float outColour;

vec3 getPosition(float depth)
{
  vec4 p_cs = inverse(projectionMatrix) * (vec4(p_vertexTexture.xy, depth, 1.0) * vec4(2.0) - vec4(1.0));
  return p_cs.xyz / p_cs.w;
}

void main(void)
{
    ivec2 texelPosition = ivec2(p_vertexTexture * screenResolution);

    vec3 normal = (viewMatrix * vec4(normalize(texelFetch(normalTexture, texelPosition, 0).xyz), 0.0)).xyz;
    vec3 position = getPosition(texelFetch(depthTexture, texelPosition, 0).r).xyz;

    float ao = 0.0;

    const vec3 offset = texture2D(ssaoNoiseTexture, p_vertexTexture * (vec2(screenResolution) / ssaoNoiseSize)).xyz;

    const vec3 tangent = normalize(offset - normal * dot(offset, normal));
    const vec3 bitangent = cross(normal, tangent);
    const mat3 tbn = mat3(tangent, bitangent, normal);
    const float r = ssaoRadius;

    vec4 samplePosition;
    vec4 screenSample;
    vec3 sampleDepth;
    float distSq;

    for (int i = 0; i < ssaoSamples; i++)
    {
        samplePosition = vec4(position + tbn * texelFetch(ssaoSamplesTexture, i, 0).xyz, 1.0);
        screenSample = projectionMatrix * samplePosition;
        screenSample.xyz = (screenSample.xyz / screenSample.w) * vec3(0.5) + vec3(0.5);

        const float depth = texelFetch(depthTexture, ivec2(screenSample.xy * screenResolution), 0).r;
        const vec4 p_cs = inverse(projectionMatrix) * (vec4(screenSample.x, screenSample.y, depth, 1.0) * vec4(2.0) - vec4(1.0));
        sampleDepth = p_cs.xyz / p_cs.w;
        distSq = dot(samplePosition.xyz - sampleDepth, samplePosition.xyz - sampleDepth);

        ao += distSq < r * r && samplePosition.z + ssaoBias < sampleDepth.z ? 1.0 : 0.0;
    }

    outColour = 1.0 - (ao / ssaoSamples);
}
