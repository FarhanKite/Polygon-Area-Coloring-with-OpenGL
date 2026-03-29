package com.raywenderlich.polygonareacoloringwithopengl.data

import com.raywenderlich.polygonareacoloringwithopengl.ToolMode

data class Circle(
    val x: Float,       // GL coordinate (-1..1)
    val y: Float,       // GL coordinate (-1..1)
    val radius: Float,  // GL units
    val mode: ToolMode  // BRUSH or ERASE
)