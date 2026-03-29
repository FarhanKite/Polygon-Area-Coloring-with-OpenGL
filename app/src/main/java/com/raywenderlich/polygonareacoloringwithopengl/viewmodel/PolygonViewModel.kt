package com.raywenderlich.polygonareacoloringwithopengl.viewmodel

import androidx.lifecycle.ViewModel
import com.raywenderlich.polygonareacoloringwithopengl.ToolMode
import com.raywenderlich.polygonareacoloringwithopengl.data.Circle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PolygonViewModel : ViewModel() {
    private val _toolMode = MutableStateFlow(ToolMode.BRUSH)
    val toolMode: StateFlow<ToolMode> = _toolMode.asStateFlow()

    private val _paintedCircles = MutableStateFlow<List<Circle>>(emptyList())
    val paintedCircles: StateFlow<List<Circle>> = _paintedCircles.asStateFlow()

    val brushRadius = 0.05f

    fun selectTool(mode: ToolMode) {
        _toolMode.value = mode
    }

    fun addCircle(x: Float, y: Float) {
        val circle = Circle(
            x = x,
            y = y,
            radius = brushRadius,
            mode = _toolMode.value
        )
        _paintedCircles.update { it + circle }
    }

    fun clearAll() {
        _paintedCircles.update { emptyList() }
    }
}
