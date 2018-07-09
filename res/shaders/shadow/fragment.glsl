#version 430 core

in vec3 p_worldPosition;

uniform float nearPlane;
uniform float farPlane;
uniform vec3 pointLightPos;

out vec4 outColour;

float getDistance(float depth)
{
    return 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));
}

void main(void)
{
    float dist = distance(p_worldPosition, pointLightPos);
    outColour = vec4(vec3(1.0 / dist), 1.0);
}
