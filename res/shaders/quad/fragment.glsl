#version 430 core

in vec3 p_vertexPosition;
in vec2 p_vertexTexture;

uniform sampler2DMS quadTextureMS;
uniform sampler2D quadTexture;
uniform samplerCube quadTextureCube;
uniform vec2 quadPosition;
uniform vec2 quadSize;
uniform bool grayscale;
uniform bool normal;
uniform bool depth;
uniform bool multisample;
uniform bool cubemap;
uniform float nearPlane;
uniform float farPlane;
uniform mat3 cubeRotation = mat3(1.0);

out vec4 outColour;

float getDistance(float depth)
{
    return 2.0 * nearPlane * farPlane / (farPlane + nearPlane - (2.0 * depth - 1.0) * (farPlane - nearPlane));
}

void main(void)
{
    vec3 colour;

    if (cubemap)
    {
        colour = vec3(texture(quadTextureCube, normalize(cubeRotation * vec3((p_vertexTexture * 2.0 - 1.0) * vec2( 1, -1), 1.0))).r);
    } else
    {
        if (multisample)
            colour = texelFetch(quadTextureMS, ivec2(p_vertexTexture * textureSize(quadTextureMS)), 0).rgb;
        else
            colour = texture2D(quadTexture, p_vertexTexture).rgb;
    }

    if (normal)
        colour.xyz = colour.xyz * 0.5 + 0.5;

    if (depth)
        colour.r = 1.0 / (1.0 + getDistance(colour.r));

    if (grayscale)
        colour.rgb = colour.rrr;

    outColour = vec4(colour, 1.0);
}
