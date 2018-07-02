#version 430 core

in vec3 p_vertexPosition;
in vec3 p_vertexNormal;
in vec2 p_vertexTexture;
in vec4 p_vertexColour;
in vec4 p_worldPosition;

uniform vec3 cameraPosition;
uniform vec4 colourMultiplier = vec4(1.0);
uniform bool wireframe;

out vec4 outDiffuse;
out vec3 outNormal;
out vec3 outPosition;
out float outSpecular;


void main(void)
{
    outDiffuse = colourMultiplier * p_vertexColour;
    outNormal = p_vertexNormal;
    outPosition = p_worldPosition.xyz;
    outSpecular = 0.0;
}
