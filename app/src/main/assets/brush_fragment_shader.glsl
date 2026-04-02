
precision mediump float;

varying vec2 vTexCoord;

uniform vec4  u_color;
uniform float u_hardness;
uniform float u_opacity;
uniform vec2  u_prevPoint;
uniform vec2  u_currPoint;
uniform float u_brushRadius;
uniform sampler2D u_mask;

void main() {
    vec2 fragPos = vTexCoord * 2.0 - 1.0;

    vec2 AB = u_currPoint - u_prevPoint;
    float lenSq = dot(AB, AB) + 0.000001;
    vec2 AP = fragPos - u_prevPoint;

    float t = clamp(dot(AP, AB) / lenSq, 0.0, 1.0);
    vec2 closest = u_prevPoint + t * AB;
    float dist = length(fragPos - closest);

    if (dist > u_brushRadius) discard;

    float normDist = dist / u_brushRadius;
//    float newAlpha = 1.0 - smoothstep(u_brushRadius * 0.5, u_brushRadius, normDist);
    // float newAlpha = 1.0 - smoothstep(0.0, 1.0, normDist);
    float newAlpha;
    if (normDist < 0.5) {
        newAlpha = 1.0;
    } else {
        newAlpha = 1.0 - smoothstep(0.0, 1.0, normDist);
    }
    newAlpha *= u_opacity;

    // READ existing mask alpha, take MAX         ← KEY CHANGE
    float existingAlpha = texture2D(u_mask, vTexCoord).a;
    float finalAlpha = max(existingAlpha, newAlpha);

    gl_FragColor = vec4(u_color.rgb, finalAlpha);
}