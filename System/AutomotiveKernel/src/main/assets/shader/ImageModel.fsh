#extension GL_OES_EGL_image_external : require
struct Materials
{
    vec4 alpha;
};

struct Textures
{
    sampler2D diffuse;
};

uniform Materials uMaterials;
uniform Textures uTextures;
uniform vec3 uViewPos;
varying vec2 vTextureCoord;

void main()
{
    gl_FragColor =  texture2D(uTextures.diffuse, vTextureCoord) * uMaterials.alpha;
}