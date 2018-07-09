#version 430 core

in vec3 vertexPosition;
in vec2 vertexTexture;

uniform vec2 quadPosition;
uniform vec2 quadSize;

out vec3 p_vertexPosition;
out vec2 p_vertexTexture;

void main(void)
{
	p_vertexPosition = vertexPosition;
    p_vertexTexture = vertexTexture;

	vec2 pos = vertexPosition.xy * vec2(quadSize.x, -quadSize.y) + vec2(quadPosition.x, quadPosition.y + quadSize.y);

	gl_Position = vec4((pos * 2.0 - 1.0) * vec2(1.0, -1.0), 0.0, 1.0);
}
