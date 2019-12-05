precision mediump sampler2D;


attribute highp vec3 aVertex;
attribute vec4 aColor;
uniform vec3 uScale;
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
varying vec4 vColor;
varying vec3 vFragPos;

void main()
{
    gl_Position = uProjection * uView * uModel * vec4(aVertex * uScale, 1.0);
    vColor = aColor;
    vFragPos = vec3(uModel * vec4(aVertex * uScale, 1.0));

}