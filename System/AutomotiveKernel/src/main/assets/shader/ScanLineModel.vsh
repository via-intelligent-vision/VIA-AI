attribute highp vec3 aVertex;
attribute highp vec4 aColor;
uniform vec3 uScale;
uniform highp mat4 uModel;
uniform highp mat4 uView;
uniform highp mat4 uProjection;
varying highp vec4 vColor;

void main()
{
    gl_Position = uProjection * uView * uModel * vec4(aVertex * uScale, 1.0);
    vColor = aColor;
}