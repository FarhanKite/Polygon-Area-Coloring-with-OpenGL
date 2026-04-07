
precision mediump float;

varying vec2 vFragPos;

uniform vec2  u_centerPoint;
uniform float u_brushRadius;
uniform vec4  u_color;
uniform float u_opacity;
uniform vec2  u_resolution;

void main() {
    float dist = length(vFragPos - u_centerPoint);

    if (dist > u_brushRadius) discard;

    float normDist = dist / u_brushRadius;

    float newAlpha = 1.0 - normDist;

    gl_FragColor = vec4(u_color.rgb, newAlpha * u_opacity * u_color.a);
}
