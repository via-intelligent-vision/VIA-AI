struct LaneDatas
{
    // data = cA * v^2 + cB * v + cC;
    //  cA = parabola.x
    //  cB = parabola.y
    //  cC = parabola.z
    //  width = parabola.w
    vec4 parabola; 
};


attribute vec3 aVertex;
attribute vec4 aColor;
uniform vec3 uScale;
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform LaneDatas laneData;
varying vec4 vColor;
varying vec3 vFragPos;

void main()
{
    vec3 anchor = aVertex;
    float cA = laneData.parabola.x;
    float cB = laneData.parabola.y;
    float cC = laneData.parabola.z;
    float width = laneData.parabola.w;
    float dir = anchor.x;
        
    float z = anchor.z;
    float tt = (cB + cA * z);
    float ex = (width / (2.0 * tt * tt + 2.0));
    anchor.x = cA * z * z + cB * z + cC + ex * dir;

    //gl_Position = uProjection * uView * uModel * vec4(aVertex * uScale, 1.0);
    gl_Position = uProjection * uView * uModel * vec4(anchor * uScale , 1.0);
    vColor = aColor;
    vFragPos = vec3(uModel * vec4(aVertex, 1.0));

}