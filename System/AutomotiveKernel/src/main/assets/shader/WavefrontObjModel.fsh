#define MAX_MTL 8

struct Materials
{
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

struct Textures
{
    sampler2D ambient;
    sampler2D diffuse;
    sampler2D specular;
    sampler2D alpha;
};

uniform Materials uMaterials;
uniform Textures uTextures;
uniform vec3 uViewPos;
uniform vec3 uLightColor;

varying highp vec3 vFragPos;
varying highp vec3 vNormal;
varying highp vec3 vColor;
varying vec2 vTextureCoord;
varying float vMaterialId;

void main()
{
    highp vec3 lightPos = vec3(0.0, 10000.0, -30000.0);
    highp vec3 norm = normalize(vNormal);

    highp vec3 ambient = uLightColor * vColor * uMaterials.ambient;


    highp vec3 lightDir = normalize(lightPos - vFragPos);
    float diff = abs(dot(norm, lightDir));
    highp vec3 diffuse = diff * uLightColor * uMaterials.diffuse;
   
    
    highp vec3 viewDir = normalize(uViewPos - vFragPos);
    highp vec3 reflectDir = reflect(-lightDir, norm);  
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), uMaterials.shininess);
    highp vec3 specular = uLightColor * (spec * uMaterials.specular);  


    diffuse = diffuse * texture2D(uTextures.diffuse, vTextureCoord).rgb;
    specular = specular * texture2D(uTextures.specular, vTextureCoord).rgb;
   
    //ambient = vec3(0.6, 0.0, 0.0);

    highp vec3 result = ambient + diffuse + specular;

    
    gl_FragColor = vec4(result, 1.0);

}