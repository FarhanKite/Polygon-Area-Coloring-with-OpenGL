package com.raywenderlich.polygonareacoloringwithopengl.viewmodel

import androidx.lifecycle.ViewModel
import com.raywenderlich.polygonareacoloringwithopengl.ToolMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PolygonViewModel : ViewModel() {

    private val _toolMode = MutableStateFlow(ToolMode.BRUSH)
    val toolMode: StateFlow<ToolMode> = _toolMode.asStateFlow()

    val brushRadius  = 0.06f
    val brushHardness = 1.0f
    val brushOpacity  = 1.0f   // 0.0 = transparent  ..  1.0 = fully opaque

    val brushColor = floatArrayOf(1f, 0f, 0f, 1f)  // red

    fun selectTool(mode: ToolMode) {
        _toolMode.value = mode
    }
}