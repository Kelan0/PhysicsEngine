#version 430 core

in vec3 p_vertexPosition;
in vec3 p_vertexNormal;
in vec2 p_vertexTexture;
in vec4 p_vertexColour;
in vec4 p_worldPosition;

uniform vec3 cameraPosition;
uniform vec4 colourMultiplier = vec4(1.0);
uniform bool wireframe;

out vec4 outColour;

const vec3 lightDirection = normalize(vec3(0.7, -1.0, 0.3));

void main(void)
{
    vec3 normal = p_vertexNormal;
    float nDotL = clamp(dot(normal, -lightDirection), 0.4, 1.0);

    vec4 colour = colourMultiplier * p_vertexColour;
    outColour = vec4(colour.rgb * nDotL, colour.a);
}
