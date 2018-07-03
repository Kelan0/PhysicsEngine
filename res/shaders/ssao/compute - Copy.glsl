#version 430 core

precision highp float;

#define SIZE_X 1
#define SIZE_Y 1

layout (local_size_x = SIZE_X, local_size_y = SIZE_Y, local_size_z = 1) in;

const float ssaoBias = 0.025;
const float root2 = 1.41421356237;
const float tau = 6.28318530718;
const float scale = 5.9;
const float intensity = 4.1;
const int iterations = 4;
const vec2 vec[] = vec2[](
    vec2(cos(tau * 0.0), sin(tau * 0.0)),
    vec2(cos(tau * 0.25), sin(tau * 0.25)),
    vec2(cos(tau * 0.50), sin(tau * 0.50)),
    vec2(cos(tau * 0.75), sin(tau * 0.75)));

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform ivec2 screenResolution;
uniform vec3 cameraPosition;
uniform vec3 cameraDirection;

uniform float nearPlane;
uniform float farPlane;

uniform int ssaoSamples;
uniform float ssaoRadius;
uniform float ssaoOffset;
uniform float ssaoTextureScale;
uniform int ssaoNoiseSize;
uniform sampler2DMS normalTexture;
uniform sampler2DMS depthTexture;
uniform sampler2D ssaoNoiseTexture;
uniform sampler1D ssaoSamplesTexture;

layout (binding = 0, r32f) uniform writeonly image2D ssaoTexture;

float getDistance(float depth)
{
    return 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));
}
vec3 getNormal(vec2 coord)
{
    return (((viewMatrix)) * vec4(normalize(texelFetch(normalTexture, ivec2(coord * screenResolution), 0).xyz), 0.0)).xyz;
}

float getDepth(vec2 coord)
{
    return texelFetch(depthTexture, ivec2(coord * screenResolution), 0).r;
}

vec3 getPosition(vec2 coord)
{
  vec4 p_cs = inverse(projectionMatrix) * (vec4(coord.x, coord.y, getDepth(coord), 1.0) * vec4(2.0) - vec4(1.0));
  return p_cs.xyz / p_cs.w;
}

vec2 getRandomOffset(vec2 coord)
{
    return normalize(texture2D(ssaoNoiseTexture, coord * (vec2(screenResolution) / ssaoNoiseSize)).xy * 2.0 - 1.0);
}

float getOcclusion(vec2 coord, vec2 offset, vec3 position, vec3 normal, float r)
{
    vec3 samplePosition = getPosition(coord + offset);
    vec3 dir = samplePosition - position;
    float dist = length(dir) * scale;

    float fragDepth = position.z;
    float sampleDepth = samplePosition.z;

    return max(0.0, dot(normal, dir) - ssaoBias) * (1.0 / (1.0 + dist)) * smoothstep(0.0, 1.0, ssaoRadius / abs(fragDepth - sampleDepth)) * intensity;
}

void main(void)
{
    vec2 textureCoord = vec2(gl_GlobalInvocationID.xy) / (ssaoTextureScale * screenResolution);

    vec3 position = getPosition(textureCoord);
    vec3 normal = getNormal(textureCoord);
    vec2 offset = getRandomOffset(textureCoord);

    float ao = 0.0;
    float r = ssaoRadius / position.z;

    for (int i = 0; i < iterations; i++)
    {
        vec2 v0 = reflect(vec[i], offset) * r;
        vec2 v1 = vec2(v0.x * root2 * 0.5 - v0.y * root2 * 0.5, v0.x * root2 * 0.5 + v0.y * root2 * 0.5);

        ao += getOcclusion(textureCoord, v0 * 0.25, position, normal, r) * 0.25;
        ao += getOcclusion(textureCoord, v1 * 0.50, position, normal, r) * 0.25;
        ao += getOcclusion(textureCoord, v0 * 0.75, position, normal, r) * 0.25;
        ao += getOcclusion(textureCoord, v1 * 1.00, position, normal, r) * 0.25;
    }

    ao = ssaoOffset + pow(1.0 - ao / iterations, 8.0) * (1.0 - ssaoOffset);

    imageStore(ssaoTexture, ivec2(gl_GlobalInvocationID.xy), vec4(vec3(ao), 1.0));
}
