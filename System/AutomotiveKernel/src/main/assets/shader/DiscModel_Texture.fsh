uniform sampler2D uTextureId;
varying vec2 vTextureCoord;


void main()
{
    vec2 tex_offset = 1.0 / textureSize(uTextureId, 0);
    vec3 result = texture2D(uTextureId, vTextureCoord).rgb;
    
    float factor = (0.5 / 81.0);
    for(int dy = 0; dy < 9; ++dy) 
    {
        for(int dx = 0; dx < 9; ++dx) {
            result += factor * texture2D(uTextureId, vTextureCoord + vec2(tex_offset.x * dx, tex_offset.y * dy)).rgb;
            result += factor * texture2D(uTextureId, vTextureCoord - vec2(tex_offset.x * dx, tex_offset.y * dy)).rgb;
        }
    }
  
    //result = texture2D(uTextureId, vTextureCoord).rgb;
    gl_FragColor = vec4(result, 1.0);
}