#version 430 core

precision highp float;

const float ssaoBias = 0.025;

uniform mat4 projectionMatrix;
uniform ivec2 screenResolution;

uniform float nearPlane;
uniform float farPlane;

uniform int ssaoSamples;
uniform float ssaoRadius;
uniform ivec2 ssaoNoiseSize;
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

void main(void)
{
    ivec2 texelPosition = ivec2(gl_GlobalInvocationID.xy);
    vec2 textureCoord = vec2(texelPosition) / vec2(screenResolution);

    vec3 normal = texelFetch(normalTexture, texelPosition, 0).xyz;;
    vec3 position = texelFetch(positionTexture, texelPosition, 0).xyz;;
    float depth = position.z;//getDistance(texelFetch(depthTexture, texelPosition, 0).x);;

    normal = normalize(normal);

    vec3 rand = texture2D(ssaoNoiseTexture, textureCoord * (screenResolution / ssaoNoiseSize)).xyz;

    vec3 tangent = normalize(rand - normal * dot(rand, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 normalMatrix = mat3(tangent, bitangent, normal);

    float ao = 0.0;

    for (int i = 0; i < ssaoSamples; ++i)
    {
        vec3 samplePoint = normalMatrix * texelFetch(ssaoSamplesTexture, i, 0).xyz;
        samplePoint = samplePoint * ssaoRadius + position;

        vec4 offset = vec4(samplePoint, 1.0);
        offset = projectionMatrix * offset;
        offset.xy /= offset.w;
        offset.xy = offset.xy * 0.5 + 0.5;

        float sampleDepth = (texelFetch(positionTexture, ivec2(offset.xy * screenResolution), 0).z);

        ao += samplePoint.z + ssaoBias <= sampleDepth ? smoothstep(0.0, 1.0, ssaoRadius / abs(depth - sampleDepth)) : 0.0;
    }

    ao = pow(1.0 - (ao / ssaoSamples), 3.0);
//
//    for (int i = 0; i < ssaoSamples; i++)
//    {
//        vec3 samplePoint = (vec4(position + normalMatrix * texelFetch(ssaoSamplesTexture, i, 0).xyz * ssaoRadius, 1.0)).xyz;
//        vec4 offset = projectionMatrix * vec4(samplePoint, 1.0);
//        offset.xyz /= offset.w;
//        offset.xyz = offset.xyz * 0.5 + 0.5;
//
//        float sampleDepth = texelFetch(positionTexture, ivec2(offset.xy * screenResolution), 0).z;
//
//        if (abs(samplePoint.z - sampleDepth) < ssaoRadius)
//            ao += step(samplePoint.z + ssaoBias, sampleDepth);// * smoothstep(0.0, 1.0, ssaoRadius / abs(position.z - sampleDepth));
//    }
//
//    ao = pow(1.0 - ao / ssaoSamples, 2.0);

    imageStore(ssaoTexture, texelPosition / 1, vec4(vec3(ao), 1.0));
}
