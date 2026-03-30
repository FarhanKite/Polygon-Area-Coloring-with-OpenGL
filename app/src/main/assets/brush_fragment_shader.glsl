precision mediump float;

varying vec2 vTexCoord;

uniform vec4  u_color;
uniform float u_hardness;
uniform float u_opacity;

void main() {
    vec2 centred = vTexCoord - vec2(0.5);
    float dist = length(centred);

    if (dist > 0.5) discard;

    float normDist = dist * 2.0;
    float hardEdge = u_hardness;
    float alpha = 1.0 - smoothstep(hardEdge, 1.0, normDist);

    alpha *= u_opacity;

    gl_FragColor = vec4(u_color.rgb, u_color.a * alpha);
}