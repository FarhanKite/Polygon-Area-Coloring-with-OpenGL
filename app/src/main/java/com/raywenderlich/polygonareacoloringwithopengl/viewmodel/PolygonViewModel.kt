package com.raywenderlich.polygonareacoloringwithopengl.viewmodel

import androidx.lifecycle.ViewModel
import com.raywenderlich.polygonareacoloringwithopengl.ToolMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PolygonViewModel : ViewModel() {

    // Currently selected tool (Brush or Erase)
    private val _toolMode = MutableStateFlow(ToolMode.BRUSH)
    val toolMode: StateFlow<ToolMode> = _toolMode.asStateFlow()

    // Brush properties — renderer reads these directly on the GL thread
    val brushRadius  = 0.06f   // GL-space radius of the stamp
    val brushHardness = 1.0f   // 0.0 = very soft edge  ..  1.0 = hard edge
    val brushOpacity  = 1.0f   // 0.0 = transparent  ..  1.0 = fully opaque

    // Paint color used in BRUSH mode (RGBA, 0..1)
    val brushColor = floatArrayOf(1f, 0f, 0f, 1f)  // red

    fun selectTool(mode: ToolMode) {
        _toolMode.value = mode
    }
}