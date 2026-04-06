//
//precision mediump float;
//
//varying vec2 vTexCoord;
//
//uniform vec4  u_color;
//uniform vec4  u_bgColor;
//uniform float u_hardness;
//uniform float u_opacity;
//uniform vec2  u_prevPoint;
//uniform vec2  u_currPoint;
//uniform float u_brushRadius;
//uniform sampler2D u_mask;
//
//void main() {
//    vec2 fragPos = vTexCoord * 2.0 - 1.0;
//
//    vec2 AB = u_currPoint - u_prevPoint;
//    float lenSq = dot(AB, AB) + 0.000001;
//    vec2 AP = fragPos - u_prevPoint;
//
//    float t = clamp(dot(AP, AB) / lenSq, 0.0, 1.0);
//    vec2 closest = u_prevPoint + t * AB;
//    float dist = length(fragPos - closest);
//
////    if (dist > u_brushRadius) discard;
//
//
//    float t2 = dot(AP, AB) / lenSq;
//    if(t2 >= 0.0 && t2 <= 1.0) {
//        float normDist = dist / u_brushRadius;
//        float newAlpha = 1.0 - normDist;
//        gl_FragColor = vec4(u_color.rgb, 0.3);
//    } else {
////        gl_FragColor = vec4(u_color.rgb, 1.0);
//    }
//
//





////    float normDist = dist / u_brushRadius;
////
////    const float threshold = 0.5;
//
////    float blendFactor = smoothstep(threshold, 1.0, normDist);
//
////    vec3 finalColor = mix(u_color.rgb, u_bgColor.rgb, blendFactor);
//
////    gl_FragColor = vec4(finalColor, u_opacity);
//
////    float newAlpha = 1.0 - normDist;
////    gl_FragColor = vec4(u_color.rgb, newAlpha);
//}
//
//
//
//
//
//
//
////
////precision mediump float;
////
////varying vec2 vTexCoord;
////
////uniform vec4  u_color;
////uniform float u_hardness;
////uniform float u_opacity;
////uniform vec2  u_prevPoint;
////uniform vec2  u_currPoint;
////uniform float u_brushRadius;
////uniform sampler2D u_mask;
////
////void main() {
////    vec2 fragPos = vTexCoord * 2.0 - 1.0;
////
////    vec2 AB = u_currPoint - u_prevPoint;
////    float len = length(AB) + 0.000001;
////    vec2 dir = AB / len;
////
/////* perpendicular direction */
////    vec2 perp = vec2(-dir.y, dir.x);
////
/////* perpendicular distance from the line axis only — NOT capsule distance */
////    vec2 AP = fragPos - u_prevPoint;
////    float perpDist = abs(dot(AP, perp));
////
/////* discard outside brush width */
////    if (perpDist > u_brushRadius) discard;
////
////    float normDist = perpDist / u_brushRadius;
////
/////* fade alpha from 1 at center to 0 at edges */
////    float alpha = 1.0 - smoothstep(0.0, 1.0, normDist);
////
////    gl_FragColor = vec4(u_color.rgb, alpha * u_opacity * u_color.a);
////}
//
//
//
//
//
////
////precision mediump float;
////
////varying vec2 vTexCoord;
////
////uniform vec4  u_color;
////uniform float u_hardness;
////uniform float u_opacity;
////uniform vec2  u_prevPoint;
////uniform vec2  u_currPoint;
////uniform float u_brushRadius;
////uniform sampler2D u_mask;
////
////void main() {
//////    vec2 fragPos = vTexCoord * 2.0 - 1.0;
////
////    vec2 fragPos = vTexCoord;
////
////    vec2 AB = u_currPoint - u_prevPoint;
////    float len = length(AB) + 0.000001;
////    vec2 dir = AB / len;
////    vec2 perp = vec2(-dir.y, dir.x);
////
////    vec2 AP = fragPos - u_prevPoint;
////
/////* distance along the segment axis */
////    float alongDist = dot(AP, dir);
////
/////* discard fragments beyond the two endpoints */
////    if (alongDist < 0.0 || alongDist > len) discard;
////
/////* perpendicular distance from the line axis */
////    float perpDist = abs(dot(AP, perp));
////
/////* discard outside brush width */
////    if (perpDist > u_brushRadius) discard;
////
////    float normDist = perpDist / u_brushRadius;
////
/////* fade alpha from 1 at center to 0 at edges */
////    float alpha = 1.0 - smoothstep(u_hardness, 1.0, normDist);
////
////    gl_FragColor = vec4(u_color.rgb, alpha * u_opacity * u_color.a);
////}









//
//precision mediump float;
//
//varying vec2 vTexCoord;
//varying vec2 vFragPos;
//
//uniform vec4  u_color;
//uniform float u_hardness;
//uniform float u_opacity;
//uniform vec2  u_prevPoint;
//uniform vec2  u_currPoint;
//uniform float u_brushRadius;
//
//void main() {
//    vec2 fragPos = vFragPos;
//
//    vec2 AB = u_currPoint - u_prevPoint;
//    float lenSq = dot(AB, AB) + 0.000001;
//    vec2 AP = fragPos - u_prevPoint;
//
//    float t = clamp(dot(AP, AB) / lenSq, 0.0, 1.0);
//    vec2 closest = u_prevPoint + t * AB;
//    float dist = length(fragPos - closest);
//
//    if (dist > u_brushRadius) discard;
//
//    float t2 = dot(AP, AB) / lenSq;
//    if (t2 < 0.0 || t2 > 1.0) discard;
//
//    float normDist = dist / u_brushRadius;
//
//    float alpha = 1.0 - smoothstep(0.0, 1.0, normDist);
//
//    gl_FragColor = vec4(u_color.rgb, alpha * u_opacity * u_color.a);
//}






//
//precision mediump float;
//
//varying vec2 vFragPos;
//
//uniform vec4  u_color;
//uniform float u_hardness;
//uniform float u_opacity;
//uniform vec2  u_prevPoint;
//uniform vec2  u_currPoint;
//uniform float u_brushRadius;
//
//void main() {
//    vec2 AB = u_currPoint - u_prevPoint;
//    float lenSq = dot(AB, AB) + 0.000001;
//    vec2 AP = vFragPos - u_prevPoint;
//
//    // How far along the segment this fragment sits (0=prevPoint, 1=currPoint)
//    float t = dot(AP, AB) / lenSq;
//
//    // Discard fragments outside the two endpoints → flat rectangle ends
//    if (t < 0.0 || t > 1.0) discard;
//
//    // Perpendicular distance from the stroke center line
//    vec2 closest = u_prevPoint + t * AB;
//    float perpDist = length(vFragPos - closest);
//
//    // Discard fragments outside the brush radius
//    if (perpDist > u_brushRadius) discard;
//
//    // normDist: 0.0 at center line, 1.0 at outer edge
//    float normDist = perpDist / u_brushRadius;
//
//    // Fade from full opacity at center to 0 at edges
//    // u_hardness=0.0 → soft (fade starts at center)
//    // u_hardness=1.0 → hard (fade only at the very edge)
//    float alpha = 1.0 - smoothstep(0.0, 1.0, normDist);
//
//    gl_FragColor = vec4(u_color.rgb, alpha * u_opacity * u_color.a);
//}



//
//
//precision mediump float;
//
//varying vec2 vFragPos;
//
//uniform vec4  u_color;
//uniform float u_hardness;
//uniform float u_opacity;
//uniform vec2  u_prevPoint;
//uniform vec2  u_currPoint;
//uniform float u_brushRadius;
//
//void main() {
//    vec2 AB = u_currPoint - u_prevPoint;
//    float lenSq = dot(AB, AB) + 0.000001;
//    vec2 AP = vFragPos - u_prevPoint;
//
//    // How far along the segment (0=prevPoint, 1=currPoint)
//    float t = dot(AP, AB) / lenSq;
//
//    // Flat rectangle ends — discard beyond endpoints
//    if (t < 0.0 || t > 1.0) discard;
//
//    // Perpendicular distance from stroke center line only
//    vec2 closest = u_prevPoint + t * AB;
//    float perpDist = length(vFragPos - closest);
//
//    // Discard outside brush width
//    if (perpDist > u_brushRadius) discard;
//
//    // 0.0 at center line, 1.0 at outer edges
//    float normDist = perpDist / u_brushRadius;
//
//    // Gradual fade across entire width:
//    // center line = alpha 1.0, outer edges = alpha 0.0
//    float alpha = 1.0 - normDist;
//
//    gl_FragColor = vec4(u_color.rgb, alpha * u_opacity * u_color.a);
//}



//
//
//precision mediump float;
//
//varying vec2 vFragPos;
//
//uniform vec2  u_centerPoint;
//uniform float u_brushRadius;
//uniform vec4  u_color;
//uniform float u_opacity;
//
//void main() {
//    float dist = length(vFragPos - u_centerPoint);
//
//    if (dist > u_brushRadius) discard;
//
//    // 1.0 at center, 0.0 at edge
//    float normDist = dist / u_brushRadius;
//    float alpha = 1.0 - normDist;
//
//    float blendFactor = smoothstep(0.5, 1.0, normDist);
//    vec3 finalColor = mix(u_color.rgb, vec3(0.9f, 0.9f, 0.9f), blendFactor);
//    gl_FragColor = vec4(finalColor.rgb, 1.0);
//
////    gl_FragColor = vec4(u_color.rgb, alpha * u_opacity * u_color.a);
//}




precision mediump float;

varying vec2 vFragPos;

uniform vec2  u_centerPoint;
uniform float u_brushRadius;
uniform vec4  u_color;
uniform float u_opacity;

void main() {
    float dist = length(vFragPos - u_centerPoint);

    if (dist > u_brushRadius) discard;

    float normDist = dist / u_brushRadius;

    // How much brush covers this fragment (1.0 at center, 0.0 at edge)
    float coverage = 1.0 - normDist;

    gl_FragColor = vec4(u_color.rgb, coverage * u_opacity * u_color.a);
}





//
//precision mediump float;
//
//varying vec2 vFragPos;
//
//uniform vec2  u_centerPoint;
//uniform float u_brushRadius;
//uniform vec4  u_color;
//uniform float u_opacity;
//
//void main() {
//    float dist = length(vFragPos - u_centerPoint);
//    if (dist > u_brushRadius) discard;
//
//    float normDist = dist / u_brushRadius;
//
//    float blendFactor = smoothstep(0.5, 1.0, normDist);
//    vec3 finalColor = mix(u_color.rgb, vec3(0.9, 0.9, 0.9), blendFactor);
//
//    float coverage = 1.0 - normDist;
//
//    gl_FragColor = vec4(finalColor * coverage, coverage);
//}