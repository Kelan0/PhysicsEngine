#version 430 core

in vec3 p_vertexPosition;
in vec2 p_vertexTexture;

uniform ivec2 screenResolution;
uniform sampler2D screenTexture;
uniform vec3 averageColour;

out vec4 outColour;

void main(void)
{
//    ivec2 textureSize = textureSize(screenTexture, 0);
//    int lod = int(log2(max(textureSize.x, textureSize.y)));
//    vec3 average = texelFetch(screenTexture, ivec2(0, 0), lod).rgb;
    vec3 average = averageColour;

    float brightness = dot(average, vec3(0.2126, 0.7152, 0.0722));

    float exposure = 1.0 / brightness;
    vec3 colour = texture2D(screenTexture, p_vertexTexture).rgb;
    vec3 hdr = vec3(1.0) - exp(-colour * exposure);

    outColour = vec4(vec3(average), 1.0);
}