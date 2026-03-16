package com.raywenderlich.polygonareacoloringwithopengl

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.raywenderlich.polygonareacoloringwithopengl.ui.theme.PolygonAreaColoringWithOpenGLTheme

class MainActivity : ComponentActivity() {

    private lateinit var renderer: PolygonRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidView(
                factory = { context ->
                    GLSurfaceView(context).apply {
                        setEGLContextClientVersion(2)
                        renderer = PolygonRenderer(context)
                        setRenderer(renderer)
                        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

                        setOnTouchListener { v, event ->
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
        }
    }
}