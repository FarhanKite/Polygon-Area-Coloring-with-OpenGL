
attribute vec4 aPosition;
attribute vec2 aTexCoord;

varying vec2 vFragPos;

void main() {
    gl_Position = aPosition;
    vFragPos    = aPosition.xy;
}