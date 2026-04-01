package com.raywenderlich.polygonareacoloringwithopengl

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.raywenderlich.polygonareacoloringwithopengl.viewmodel.PolygonViewModel

class MainActivity : ComponentActivity() {

    private lateinit var renderer: PolygonRenderer
    private lateinit var glSurfaceView: GLSurfaceView
    private val viewModel: PolygonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val toolMode by viewModel.toolMode.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        GLSurfaceView(context).apply {
                            setEGLContextClientVersion(2)
                            setEGLConfigChooser(8, 8, 8, 8, 16, 8)

                            renderer = PolygonRenderer(context, viewModel)
                            setRenderer(renderer)
                            renderer.attachSurface(this)

                            glSurfaceView = this

                            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

                            setOnTouchListener { _, event ->
                                val x = event.x
                                val y = event.y
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                        renderer.handleTouch(x, y, width, height)
                                    }
                                    MotionEvent.ACTION_UP -> {
                                        renderer.resetTouchedVertexIndex()
                                    }
                                }
                                true
                            }
                        }
                    }
                )

                BrushEraseToolbar(
                    selectedMode = toolMode,
                    onModeSelected = viewModel::selectTool,
                    onUndo = { glSurfaceView.queueEvent { renderer.undo() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::renderer.isInitialized && ::glSurfaceView.isInitialized) {
            glSurfaceView.queueEvent { renderer.saveMaskPixels() }
            glSurfaceView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::glSurfaceView.isInitialized) {
            glSurfaceView.onResume()
        }
    }
}