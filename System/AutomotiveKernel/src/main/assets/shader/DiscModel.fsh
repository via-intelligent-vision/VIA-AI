precision highp float;
precision highp int;
precision highp sampler2D;

struct Material
{
    vec3 ambient;
};
uniform Material uMaterial;
uniform vec3 uLightPos;
varying vec4 vColor;
varying vec3 vFragPos;

void main()
{
    vec3 norm = normalize(vec3(0.0, 1.0, 0.0));

    vec3 lightDir = normalize(uLightPos - vFragPos);
    float diff = abs(dot(norm, lightDir));

    vec4 result = vColor * vec4(diff * uMaterial.ambient, 1.0);

    gl_FragColor = result;
}