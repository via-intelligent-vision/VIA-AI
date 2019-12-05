attribute vec3 aVertex;
attribute vec2 aTextCoord;
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
varying vec2 vTextureCoord;

void main()
{
    gl_Position = uProjection * uView * uModel * vec4(aVertex, 1.0);
    vTextureCoord = aTextCoord;
}