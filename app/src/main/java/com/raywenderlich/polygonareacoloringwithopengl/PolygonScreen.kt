package com.raywenderlich.polygonareacoloringwithopengl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raywenderlich.polygonareacoloringwithopengl.viewmodel.PolygonViewModel

@Composable
fun PolygonScreen(
    viewModel: PolygonViewModel = viewModel(),
) {
    val toolMode by viewModel.toolMode.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PolygonGLSurfaceViewWrapper(
            modifier = Modifier.fillMaxSize()
        )

        BrushEraseToolbar(
            selectedMode = toolMode,
            onModeSelected = viewModel::selectTool,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}