attribute vec3 aVertex;
attribute highp vec3 aNormal;
attribute highp vec3 aColor;
attribute vec2 aTextCoord;
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
varying highp vec3 vFragPos;
varying highp vec3 vNormal;
varying highp vec3 vColor;
varying vec2 vTextureCoord;

void main()
{
    gl_Position = uProjection * uView * uModel * vec4(aVertex, 1.0);
    vColor = aColor;
    vNormal = normalize(aNormal);
    vTextureCoord = aTextCoord;
    vFragPos = vec3(uModel * vec4(aVertex, 1.0));

}