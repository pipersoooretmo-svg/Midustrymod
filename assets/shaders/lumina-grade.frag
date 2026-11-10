#define HIGHP

uniform sampler2D u_texture;
uniform float u_grade;
uniform float u_vignette;

varying vec2 v_texCoords;

void main(){
    vec3 col = texture2D(u_texture, v_texCoords).rgb;

    //gentle S-curve for contrast
    vec3 curved = col * col * (3.0 - 2.0 * col);
    col = mix(col, curved, 0.35 * u_grade);

    //richer colours
    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.0 + 0.18 * u_grade);

    //split toning: warm highlights, cool shadows
    col += (vec3(0.022, 0.008, -0.014) * luma + vec3(-0.01, 0.0, 0.02) * (1.0 - luma)) * u_grade;

    vec2 d = v_texCoords - 0.5;
    float edge = smoothstep(0.38, 1.0, length(d) * 1.42);
    col *= 1.0 - edge * u_vignette;

    gl_FragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}
