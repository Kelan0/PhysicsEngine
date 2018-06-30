#version 430 core

in vec3 vertexPosition;
in vec3 vertexNormal;
in vec2 vertexTexture;
in vec4 vertexColour;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out vec3 p_vertexPosition;
out vec3 p_vertexNormal;
out vec2 p_vertexTexture;
out vec4 p_vertexColour;
out vec4 p_worldPosition;

void main(void)
{
    vec4 worldPosition = viewMatrix * modelMatrix * vec4(vertexPosition, 1.0);

    p_vertexPosition = vertexPosition;
    p_vertexNormal = vertexNormal;
    p_vertexTexture = vertexTexture;
    p_vertexColour = vertexColour;
    p_worldPosition = worldPosition;

    gl_Position = projectionMatrix * worldPosition;
}
