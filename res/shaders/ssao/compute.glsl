#version 430 core

precision highp float;

#define SIZE_X 1
#define SIZE_Y 1

layout (local_size_x = SIZE_X, local_size_y = SIZE_Y, local_size_z = 1) in;

const float ssaoBias = 0.025;
const float root2 = 1.41421356237;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform ivec2 screenResolution;
uniform vec3 cameraPosition;
uniform vec3 cameraDirection;

uniform float nearPlane;
uniform float farPlane;

uniform int ssaoSamples;
uniform float ssaoRadius;
uniform int ssaoNoiseSize;
uniform sampler2DMS normalTexture;
uniform sampler2DMS positionTexture;
uniform sampler2DMS depthTexture;
uniform sampler2D ssaoNoiseTexture;
uniform sampler1D ssaoSamplesTexture;

layout (binding = 0, r32f) uniform writeonly image2D ssaoTexture;

float getDistance(float depth)
{
    return 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));
}

vec3 getPosition(ivec2 coord)
{
    return (((viewMatrix)) * vec4(texelFetch(positionTexture, coord, 0).xyz, 1.0)).xyz;
}

vec3 getNormal(ivec2 coord)
{
    return (((viewMatrix)) * vec4(normalize(texelFetch(normalTexture, coord, 0).xyz), 0.0)).xyz;
}

vec2 getRandomOffset(ivec2 coord)
{
    return normalize(texture2D(ssaoNoiseTexture, (vec2(coord) / vec2(screenResolution)) * (vec2(screenResolution) / ssaoNoiseSize)).xy * 2.0 - 1.0);
}

float getOcclusion(ivec2 coord, ivec2 offset, vec3 position, vec3 normal, float r)
{
    const float scale = 1.0;
    const float intensity = 1.4;

    vec3 samplePosition = getPosition(coord + offset);
    vec3 dir = samplePosition - position;
    float dist = length(dir) * scale;

    float fragDepth = position.z;
    float sampleDepth = samplePosition.z;

    return max(0.0, dot(normal, dir) - ssaoBias) * (1.0 / (1.0 + dist)) * smoothstep(0.0, 1.0, ssaoRadius / abs(fragDepth - sampleDepth)) * intensity;
}

void main(void)
{
    ivec2 texelPosition = ivec2(gl_GlobalInvocationID.xy);

    vec3 position = getPosition(texelPosition);
    vec3 normal = getNormal(texelPosition);
    vec2 offset = getRandomOffset(texelPosition);

    vec2 vec[] = vec2[](vec2(+1.0, 0.0), vec2(-1.0, 0.0), vec2(0.0, +1.0), vec2(0.0, -1.0));
    float ao = 0.0;
    float r = ssaoRadius / position.z;

    int iterations = 4;
    for (int i = 0; i < iterations; i++)
    {
        vec2 v0 = reflect(vec[i % 4], offset) * r;
        vec2 v1 = vec2(v0.x * root2 * 0.5 - v0.y * root2 * 0.5, v0.x * root2 * 0.5 + v0.y * root2 * 0.5);

        ao += getOcclusion(texelPosition, ivec2(v0 * screenResolution * 0.25), position, normal, r) * 0.25;
        ao += getOcclusion(texelPosition, ivec2(v1 * screenResolution * 0.50), position, normal, r) * 0.25;
        ao += getOcclusion(texelPosition, ivec2(v0 * screenResolution * 0.75), position, normal, r) * 0.25;
        ao += getOcclusion(texelPosition, ivec2(v1 * screenResolution * 1.00), position, normal, r) * 0.25;
    }

    ao = 1.0 - pow(1.0 - ao / iterations, 3.0);

    imageStore(ssaoTexture, texelPosition, vec4(vec3(ao), 1.0));
}
