
precision mediump float;

varying vec2 vTexCoord;

uniform vec4  u_color;
uniform float u_hardness;
uniform float u_opacity;
uniform vec2 u_prevPoint;
uniform vec2 u_currPoint;
uniform float u_brushRadius;

void main() {
    vec2 AB = u_currPoint - u_prevPoint;
    float len = length(AB) + 0.0001; // avoid division by zero
    vec2 AP = vTexCoord - u_prevPoint;

    // Perpendicular distance from fragment to line
    float dist = abs(AB.x * AP.y - AB.y * AP.x) / len;

    if(dist > u_brushRadius) discard;

    float normDist = dist / u_brushRadius;
    float hardEdge = u_hardness;
    float alpha = 1.0 - smoothstep(hardEdge, 1.0, normDist);
    alpha *= u_opacity;

    gl_FragColor = vec4(u_color.rgb, u_color.a * alpha);
}