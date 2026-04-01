package com.raywenderlich.polygonareacoloringwithopengl.input

import android.R.attr.x
import com.raywenderlich.polygonareacoloringwithopengl.data.PolygonData.vertices
import com.raywenderlich.polygonareacoloringwithopengl.geometry.PolygonTriangulator
import com.raywenderlich.polygonareacoloringwithopengl.gl.CoordUtils
import kotlin.math.pow
import kotlin.math.sqrt

class TouchHandler(
    private val onVertexMoved: (triangulatedVertices: FloatArray) -> Unit,
    private val onPaintRequest: (prevGlX: Float, prevGlY: Float, glX: Float, glY: Float) -> Unit,
    private val onStrokeEnded: () -> Unit
) {

    private var selectedVertexIndex: Int? = null
    private var isPainting = false

    fun onTouch(prevX: Float, prevY: Float, x: Float, y: Float, width: Int, height: Int, triangulatedVertices: FloatArray) {
        val (glX, glY) = CoordUtils.screenToGL(x, y, width, height)

        selectedVertexIndex?.let { idx ->
            vertices[idx] = glX
            vertices[idx + 1] = glY
            onVertexMoved(PolygonTriangulator.triangulate(vertices))
            return
        }

        if (!isPainting) {
            for (i in vertices.indices step 2) {
                val dist = sqrt((glX - vertices[i]).pow(2) + (glY - vertices[i + 1]).pow(2))
                if (dist < TOUCH_RADIUS) {
                    selectedVertexIndex = i
                    return
                }
            }
        }

        if (PolygonTriangulator.isPointInsideTriangulation(glX, glY, triangulatedVertices)) {
            isPainting = true
            val (prevGlX, prevGlY) = CoordUtils.screenToGL(prevX, prevY, width, height)
            onPaintRequest(prevGlX, prevGlY, glX, glY)
        }
    }

    fun setLastTouchedPoint(x: Float, y: Float) {

    }

    fun onTouchUp() {
        if (isPainting) onStrokeEnded()
        selectedVertexIndex = null
        isPainting = false
    }

    companion object {
        private const val TOUCH_RADIUS = 0.1f
    }
}