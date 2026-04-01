package com.raywenderlich.polygonareacoloringwithopengl.gl

object CoordUtils {

    fun screenToGL(x: Float, y: Float, width: Int, height: Int): Pair<Float, Float> {
        val glX = (x / width) * 2f - 1f
        val glY = -((y / height) * 2f - 1f)
        return glX to glY
    }
}