struct Material
{
    vec3 diffuse;
};
uniform Material uMaterial;
varying vec4 vColor;

void main()
{
    vec4 result = vColor * vec4(uMaterial.diffuse, 1.0);
    gl_FragColor = result;
}