struct Material
{
    vec3 ambient;
};
uniform Material uMaterial;
varying highp vec4 vColor;

void main()
{
    highp vec4 result = vColor * vec4(uMaterial.ambient, 1.0);

    gl_FragColor = result;
}