#version 430 core

#define MAX_SHADOWS 16
precision highp float;

const float epsilon = 0.05;

const vec3 sampleOffsetDirections[20] = vec3[]
(
   vec3( 1,  1,  1), vec3( 1, -1,  1), vec3(-1, -1,  1), vec3(-1,  1,  1),
   vec3( 1,  1, -1), vec3( 1, -1, -1), vec3(-1, -1, -1), vec3(-1,  1, -1),
   vec3( 1,  1,  0), vec3( 1, -1,  0), vec3(-1, -1,  0), vec3(-1,  1,  0),
   vec3( 1,  0,  1), vec3(-1,  0,  1), vec3( 1,  0, -1), vec3(-1,  0, -1),
   vec3( 0,  1,  1), vec3( 0, -1,  1), vec3( 0, -1, -1), vec3( 0,  1, -1)
);

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

uniform Light light;

uniform ivec2 screenResolution;
uniform sampler2DMS depthTexture;

out vec4 outColour;

vec3 getPosition(float depth)
{
  vec4 p_cs = inverse(projectionMatrix) * (vec4(p_vertexTexture.xy, depth, 1.0) * vec4(2.0) - vec4(1.0));
  return (inverse(viewMatrix) * vec4(p_cs.xyz / p_cs.w, 1.0)).xyz;
}

float getDistance(float depth)
{
    return 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));
}

float getShadowFactor(vec3 surfacePosition)
{
    vec3 lightDirection = surfacePosition - light.position;
    float surfaceDistance = length(lightDirection);

    float attenuation = 1.0;// / (light.attenuation.x + light.attenuation.y * surfaceDistance + light.attenuation.z * surfaceDistance * surfaceDistance);

    float shadow = 0.0;

    if (attenuation > 0.001)
    {
        float cameraDistance = length(cameraPosition - surfacePosition);

        float diskRadius = (1.0 + (cameraDistance / 256.0)) / 25.0;
        for (int i = 0; i < 20; i++)
        {
            float sampleDistance = 1.0 / texture(light.shadowMap, lightDirection * vec3(-1, -1, +1) + sampleOffsetDirections[i] * diskRadius).r;
            shadow += surfaceDistance < sampleDistance + epsilon ? 1.0 : 0.0;
        }
    }

    return clamp(shadow / 20, 0.0, 1.0) * attenuation;
}

void main(void)
{
    vec3 position = getPosition(texelFetch(depthTexture, ivec2(p_vertexTexture * screenResolution), 0).r);
    outColour = vec4(getShadowFactor(position));
}
