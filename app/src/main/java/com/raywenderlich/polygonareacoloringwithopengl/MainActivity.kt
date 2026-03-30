//package com.raywenderlich.polygonareacoloringwithopengl
//
//import android.opengl.GLSurfaceView
//import android.os.Bundle
//import android.view.MotionEvent
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.viewModels
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.viewinterop.AndroidView
//import com.raywenderlich.polygonareacoloringwithopengl.ui.theme.PolygonAreaColoringWithOpenGLTheme
//import com.raywenderlich.polygonareacoloringwithopengl.viewmodel.PolygonViewModel
//
//class MainActivity : ComponentActivity() {
//
//    private lateinit var renderer: PolygonRenderer
//    private val viewModel: PolygonViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            val toolMode by viewModel.toolMode.collectAsState()
//
//            Box(modifier = Modifier.fillMaxSize()) {
//                AndroidView(
//                    factory = { context ->
//                        GLSurfaceView(context).apply {
//                            setEGLContextClientVersion(2)
//
//                            setEGLConfigChooser(8, 8, 8, 8, 16, 8)
//
//                            renderer = PolygonRenderer(context, viewModel)
//                            setRenderer(renderer)
//                            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
//
//                            setOnTouchListener { v, event ->
//                                val x = event.x
//                                val y = event.y
//
//                                when (event.action) {
//                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
//                                        renderer.handleTouch(x, y, width, height)
//                                    }
//
//                                    MotionEvent.ACTION_UP -> {
//                                        renderer.resetTouchedVertexIndex()
//                                    }
//                                }
//
//                                true
//                            }
//                        }
//                    }
//                )
//
//                BrushEraseToolbar(
//                    selectedMode   = toolMode,
//                    onModeSelected = viewModel::selectTool,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.BottomCenter)
//                )
//            }
//        }
//    }
//}








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

                            // IMPORTANT: pass the view reference so the renderer
                            // can call queueEvent() to stamp brushes on the GL thread
                            renderer.attachSurface(this)

                            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

                            setOnTouchListener { v, event ->
                                val x = event.x
                                val y = event.y

                                when (event.action) {
                                    MotionEvent.ACTION_DOWN,
                                    MotionEvent.ACTION_MOVE -> {
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
                    selectedMode   = toolMode,
                    onModeSelected = viewModel::selectTool,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}