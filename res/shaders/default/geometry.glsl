#version 430 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

in vec3 p0_vertexPosition[3];
in vec3 p0_vertexNormal[3];
in vec2 p0_vertexTexture[3];
in vec4 p0_vertexColour[3];
in vec4 p0_worldPosition[3];

out vec3 p1_vertexPosition;
out mat3 p1_normalMatrix;
out vec2 p1_vertexTexture;
out vec4 p1_vertexColour;
out vec4 p1_worldPosition;

void main(void)
{
    mat3 mat = mat3(modelMatrix);
    vec3 edge0 = p0_worldPosition[1].xyz - p0_worldPosition[0].xyz;
    vec3 edge1 = p0_worldPosition[2].xyz - p0_worldPosition[0].xyz;

    vec2 duv0 = p0_vertexTexture[1].xy - p0_vertexTexture[0].xy;
    vec2 duv1 = p0_vertexTexture[2].xy - p0_vertexTexture[0].xy;

    float r = 1.0 / (duv0.x * duv1.y - duv0.y * duv1.x);
    vec3 tangent = normalize(mat * vec3(r * (edge0 * duv1.y - edge1 * duv0.y)));
    vec3 bitangent = normalize(mat * vec3(r * (edge1 * duv0.x - edge0 * duv1.x)));

    for(int i = 0; i < 3; i++)
    {
        p1_vertexPosition = (modelMatrix * viewMatrix * vec4(p0_vertexPosition[i], 0.0)).xyz;
        p1_normalMatrix = mat3(tangent, bitangent, normalize(p0_vertexNormal[i]));
        p1_vertexTexture = p0_vertexTexture[i];
        p1_vertexColour = p0_vertexColour[i];
        p1_worldPosition = p0_worldPosition[i];

        gl_Position = gl_in[i].gl_Position;
        EmitVertex();
    }
    EndPrimitive();
}
