#version 430 core

in vec3 vertexPosition;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out vec3 p_worldPosition;

void main(void)
{
    vec4 worldPosition = modelMatrix * vec4(vertexPosition, 1.0);
	p_worldPosition = worldPosition.xyz;
	gl_Position = projectionMatrix * viewMatrix * worldPosition;
}
