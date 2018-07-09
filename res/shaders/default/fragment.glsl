#version 430 core

in vec3 p1_vertexPosition;
in mat3 p1_normalMatrix;
in vec2 p1_vertexTexture;
in vec4 p1_vertexColour;
in vec4 p1_worldPosition;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 cameraPosition;
uniform vec3 cameraDirection;
uniform float nearPlane;
uniform float farPlane;
uniform vec4 colourMultiplier = vec4(1.0);
uniform vec3 ambientColour = vec3(1.0);
uniform vec3 diffuseColour = vec3(1.0);
uniform vec3 specularColour = vec3(1.0);
uniform float specularPower = 1.0;
uniform float transmission = 0.0;
uniform vec3 transmissionFilter = vec3(1.0);
uniform float opticalDensity = 1.0;

uniform sampler2D ambientColourTexture;
uniform sampler2D diffuseColourTexture;
uniform sampler2D specularColourTexture;
uniform sampler2D specularPowerTexture;
uniform sampler2D displacementTexture;
uniform sampler2D normalTexture;
uniform sampler2D alphaTexture;

uniform bool drawWireframe;
uniform bool drawGeometry;
uniform bool displacementMap;
uniform bool normalMap;
uniform bool alphaMap;
uniform bool clampUV;

out vec4 outDiffuse;
out vec3 outNormal;
out vec3 outPosition;
out vec4 outSpecular;

bool getParallax(inout vec2 texturePosition)
{
    const int minLayers = 10;
    const int maxLayers = 10;

    const mat3 tbn = transpose(p1_normalMatrix);
    const vec3 surfaceToCamera = normalize(tbn * (cameraPosition - p1_worldPosition.xyz));
    const float layerDepth = 0.1;//int(mix(maxLayers, minLayers, abs(dot(vec3(0.0, 0.0, 1.0), surfaceToCamera))));
    const float scale = 0.04;

    const vec2 offset = (surfaceToCamera.xy / surfaceToCamera.z) * scale * layerDepth;

    float currLayerDepth = 0.0;
    float currMapDepth = 1.0;

    do
    {
        texturePosition -= offset;
        currMapDepth = 1.0 - texture2D(displacementTexture, texturePosition).r;
        currLayerDepth += layerDepth;
    } while (currLayerDepth < currMapDepth);

    vec2 prevTexturePosition = texturePosition + offset;
    float afterDepth = currMapDepth - currLayerDepth;
    float beforeDepth = (1.0 - texture2D(displacementTexture, prevTexturePosition).r) - currLayerDepth + layerDepth;

    float weight = afterDepth / (afterDepth - beforeDepth);
    texturePosition = prevTexturePosition * weight + texturePosition * (1.0 - weight);

    vec4 position = projectionMatrix * viewMatrix * vec4(p1_worldPosition.xyz + tbn * vec3(0.0, 0.0, currMapDepth * scale), 1.0);
    float depth = (position.z / position.w) * 0.5 + 0.5;
    gl_FragDepth = gl_DepthRange.diff * depth + gl_DepthRange.near;

    return true;//texturePosition.x >= 0.0 && texturePosition.y >= 0.0 && texturePosition.x < 1.0 && texturePosition.y < 1.0;
}

void main(void)
{
    vec2 texturePosition = p1_vertexTexture;
    float parallaxHeight;
//    gl_FragDepth = gl_FragCoord.z;
//
//    if (displacementMap && !getParallax(texturePosition))
//        discard;

    if ((alphaMap && texture2D(alphaTexture, texturePosition).r < 0.5))
        discard;

    if (normalMap)
    {
        outNormal = p1_normalMatrix * (texture2D(normalTexture, texturePosition).xyz * 2.0 - 1.0);
    } else
    {
        outNormal = p1_normalMatrix * vec3(0.0, 0.0, 1.0);
    }

    vec4 diffuse = texture2D(diffuseColourTexture, texturePosition);

    outDiffuse = colourMultiplier * p1_vertexColour * vec4(diffuseColour, 1.0) * diffuse;
    outPosition = p1_worldPosition.xyz;
    outSpecular = vec4(texture2D(specularColourTexture, texturePosition).rgb, specularPower);
}
