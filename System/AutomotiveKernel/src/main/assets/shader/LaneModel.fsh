struct Material
{
    highp vec3 ambient;
    highp vec3 diffuse;
};
uniform Material uMaterial;
uniform vec3 uLightPos;
varying highp vec4 vColor;
varying highp vec3 vFragPos; 

void main()
{
    highp vec3 norm = normalize(vec3(0.0, 1.0, 0.0));

    highp vec4 result;
    if(uLightPos.y > 0.0) {
        highp vec3 lightDir = normalize(uLightPos - vFragPos);
        highp float diff = abs(dot(norm, lightDir));
        
        result = ((1.0 - diff) * vColor + diff * vec4(uMaterial.diffuse, vColor.a * 1.5)) * vec4(uMaterial.ambient, 1.0);
    }
    else {
        result = vColor * vec4(uMaterial.ambient * uMaterial.diffuse, 1.0);
    }

    gl_FragColor = result;

    //vec4 result = vColor * vec4(uMaterial.ambient, 1.0);
    //gl_FragColor = result;
}