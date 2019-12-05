attribute vec3 aVertex;
attribute vec4 aColor;
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
varying vec4 vColor;

void main()
{
    gl_Position = uProjection * uView * uModel * vec4(aVertex, 1.0);
    vColor = aColor;
}