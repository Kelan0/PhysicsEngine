#version 430 core

precision highp float;

#define SIZE_X 1
#define SIZE_Y 1

layout (local_size_x = SIZE_X, local_size_y = SIZE_Y, local_size_z = 1) in;

const float ssaoBias = 0.05;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform ivec2 screenResolution;
uniform vec3 cameraPosition;
uniform vec3 cameraDirection;

uniform float nearPlane;
uniform float farPlane;

uniform bool drawWireframe;
uniform bool drawGeometry;
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
    return (viewMatrix * vec4(normalize(texelFetch(normalTexture, ivec2(coord * screenResolution), 0).xyz), 0.0)).xyz;
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

vec3 getRandomOffset(vec2 coord)
{
    return texture2D(ssaoNoiseTexture, coord * (vec2(screenResolution) / ssaoNoiseSize)).xyz;
}

mat3 getNormalMatrix(vec3 normal, vec3 rotation)
{
    vec3 tangent = normalize(rotation - normal * dot(rotation, normal));
    vec3 bitangent = cross(normal, tangent);
    return mat3(tangent, bitangent, normal);
}

void main(void)
{
    float ao = 0.0;

    if (drawGeometry)
    {
        const vec2 textureCoord = vec2(gl_GlobalInvocationID.xy) / (ssaoTextureScale * screenResolution);

        const vec3 position = getPosition(textureCoord);
        const vec3 normal = getNormal(textureCoord);
        const vec3 offset = getRandomOffset(textureCoord);

        const mat3 tbn = getNormalMatrix(normal, offset);
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

            sampleDepth = getPosition(screenSample.xy);
            distSq = dot(samplePosition.xyz - sampleDepth, samplePosition.xyz - sampleDepth);

    //        if (samplePosition.z + ssaoBias < sampleDepth.z)
    //            if (distSq < r * r)
    //                ao += smoothstep(0.0, 1.0, r / abs(samplePosition.z - sampleDepth.z)) * (1.0 / (1.0 + sqrt(distSq)));
            ao += distSq < r * r && samplePosition.z + ssaoBias < sampleDepth.z ? 1.0 : 0.0;
        }
    }

    imageStore(ssaoTexture, ivec2(gl_GlobalInvocationID.xy), vec4(vec3(1.0 - (ao / ssaoSamples)), 1.0));
}
