#version 430 core

in vec3 vertexPosition;
in vec3 vertexNormal;
in vec2 vertexTexture;
in vec4 vertexColour;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out vec3 p0_vertexPosition;
out vec3 p0_vertexNormal;
out vec2 p0_vertexTexture;
out vec4 p0_vertexColour;
out vec4 p0_worldPosition;

void main(void)
{
    vec4 worldPosition = modelMatrix * vec4(vertexPosition, 1.0);

    p0_vertexPosition = vertexPosition;
    p0_vertexNormal = vertexNormal;
    p0_vertexTexture = vertexTexture;
    p0_vertexColour = vertexColour;
    p0_worldPosition = worldPosition;

    gl_Position = projectionMatrix * viewMatrix * worldPosition;
}
