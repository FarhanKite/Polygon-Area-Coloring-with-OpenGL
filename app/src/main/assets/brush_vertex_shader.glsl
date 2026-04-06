//
//attribute vec4 aPosition;
//attribute vec2 aTexCoord;
//
//varying vec2 vTexCoord;
//
//void main() {
//    gl_Position = aPosition;
//    vTexCoord   = aTexCoord;
//}


//
//attribute vec4 aPosition;
//attribute vec2 aTexCoord;
//
//varying vec2 vTexCoord;
//varying vec2 vFragPos;
//
//void main() {
//    gl_Position = aPosition;
//    vTexCoord   = aTexCoord;
//    vFragPos    = aPosition.xy;
//}




attribute vec4 aPosition;
attribute vec2 aTexCoord;

varying vec2 vFragPos;

void main() {
    gl_Position = aPosition;
    vFragPos    = aPosition.xy;
}