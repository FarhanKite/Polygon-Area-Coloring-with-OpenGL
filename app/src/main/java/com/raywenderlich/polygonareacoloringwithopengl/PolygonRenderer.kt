package com.raywenderlich.polygonareacoloringwithopengl

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.ui.input.key.Key
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import com.raywenderlich.polygonareacoloringwithopengl.data.PolygonData
import com.raywenderlich.polygonareacoloringwithopengl.data.PolygonData.vertices
import com.raywenderlich.polygonareacoloringwithopengl.viewmodel.PolygonViewModel
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class PolygonRenderer(
    private val context: Context,
    private val viewModel: PolygonViewModel
) : GLSurfaceView.Renderer {

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var triangleBuffer: FloatBuffer
    private var positionHandle = 0
    private var colorHandle = 0
    private var program = 0
    private var triangulatedVertices = FloatArray(0)

    private val circleSegments = 40

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 1f)

        vertexBuffer = ByteBuffer
            .allocateDirect(vertices.size * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        triangulatedVertices = triangulatePolygon(vertices)
        triangleBuffer = createFloatBuffer(triangulatedVertices)

        val vertexShaderCode = ShaderUtils.loadShaderFromAssets(context, "vertex_shader.glsl")
        val fragmentShaderCode = ShaderUtils.loadShaderFromAssets(context, "fragment_shader.glsl")

        val vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)

        val linkStatus = IntArray(1)
        glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = glGetProgramInfoLog(program)
            glDeleteProgram(program)
            throw RuntimeException("Program link failed: $error")
        }

        positionHandle = glGetAttribLocation(program, "aPosition")
        colorHandle = glGetUniformLocation(program, "uColor")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear both color and stencil every frame
        glClear(GL_COLOR_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

        glUseProgram(program)

        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_ALWAYS, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
        glColorMask(false, false, false, false)

        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, triangleBuffer)
        glDrawArrays(GL_TRIANGLES, 0, triangulatedVertices.size / 2)
        glDisableVertexAttribArray(positionHandle)

        glStencilFunc(GL_EQUAL, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glColorMask(true, true, true, true)

        // draw polygon
//        glUseProgram(program)
        glUniform4f(colorHandle, 1f, 0f, 0f, 1f)
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, triangleBuffer)
        glDrawArrays(GL_TRIANGLES, 0, triangulatedVertices.size / 2)
        glDisableVertexAttribArray(positionHandle)

        val circles = viewModel.paintedCircles.value
        for (circle in circles) {
            if (circle.mode == ToolMode.BRUSH) {
                drawCircle(circle.x, circle.y, circle.radius, 1f, 0f, 0f, 1f)
            } else {
                drawCircle(circle.x, circle.y, circle.radius, 1f, 1f, 1f, 1f)
            }
        }

        glDisable(GL_STENCIL_TEST)

//        // draw line
//        glUniform4f(colorHandle, 1f, 1f, 0f, 1f) // yellow outline
//        glEnableVertexAttribArray(positionHandle)
//        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, vertexBuffer)
//        glDrawArrays(GL_LINE_LOOP, 0, vertices.size / 2)
//        glDisableVertexAttribArray(positionHandle)

        // draw points
//        glUseProgram(program)
        glUniform4f(colorHandle, 0f, 1f, 0f, 1f)
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, vertexBuffer)
        glDrawArrays(GL_POINTS, 0, vertices.size / 2)
        glDisableVertexAttribArray(positionHandle)
    }

    private fun drawCircle(
        cx: Float, cy: Float, radius: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        val coords = FloatArray((circleSegments + 2) * 2)
        // centre point
        coords[0] = cx
        coords[1] = cy
        // surrounding points
        for (i in 0..circleSegments) {
            val angle = (2.0 * Math.PI * i / circleSegments).toFloat()
            coords[(i + 1) * 2]     = cx + radius * cos(angle)
            coords[(i + 1) * 2 + 1] = cy + radius * sin(angle)
        }

        val buffer = createFloatBuffer(coords)

        glUniform4f(colorHandle, r, g, b, a)
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, buffer)
        glDrawArrays(GL_TRIANGLE_FAN, 0, circleSegments + 2)
        glDisableVertexAttribArray(positionHandle)
    }

    private fun triangulatePolygon(verts: FloatArray): FloatArray {
        val n = verts.size / 2
        if (n < 3) return FloatArray(0)

        val indices = ArrayDeque<Int>((0 until n).toList())

        if (!isCounterClockwise(verts, indices)) {
            indices.reverse()
        }

        val result = mutableListOf<Float>()

        var safetyCounter = 0
        val maxIterations = n * n

        while (indices.size > 3 && safetyCounter < maxIterations) {
            var earFound = false
            val size = indices.size

            for (i in 0 until size) {
                val prevIdx = indices[(i - 1 + size) % size]
                val currIdx = indices[i]
                val nextIdx = indices[(i + 1) % size]

                if (isEar(verts, indices, prevIdx, currIdx, nextIdx)) {
                    result.addTriangle(verts, prevIdx, currIdx, nextIdx)
                    indices.removeAt(i)
                    earFound = true
                    break
                }
            }

            if (!earFound) {
                indices.removeAt(0)
            }

            safetyCounter++
        }

        if (indices.size == 3) {
            result.addTriangle(verts, indices[0], indices[1], indices[2])
        }

        return result.toFloatArray()
    }

    // Returns true if the polygon vertices are wound counter-clockwise.
    // Uses the shoelace formula to compute signed area.
    private fun isCounterClockwise(verts: FloatArray, indices: ArrayDeque<Int>): Boolean {
        var signedArea = 0f
        val n = indices.size
        for (i in 0 until n) {
            val curr = indices[i]
            val next = indices[(i + 1) % n]
            val x0 = verts[curr * 2]
            val y0 = verts[curr * 2 + 1]
            val x1 = verts[next * 2]
            val y1 = verts[next * 2 + 1]
            signedArea += (x0 * y1) - (x1 * y0)
        }
        return signedArea > 0f
    }

    // Checks if vertex [curr] is an "ear" tip:
    // 1. The triangle (prev, curr, next) must be convex (left turn in CCW polygon).
    // 2. No other polygon vertex lies inside this triangle.
    private fun isEar(
        verts: FloatArray,
        indices: ArrayDeque<Int>,
        prevIdx: Int,
        currIdx: Int,
        nextIdx: Int
    ): Boolean {
        val ax = verts[prevIdx * 2];  val ay = verts[prevIdx * 2 + 1]
        val bx = verts[currIdx * 2];  val by = verts[currIdx * 2 + 1]
        val cx = verts[nextIdx * 2];  val cy = verts[nextIdx * 2 + 1]

        // Must be a convex vertex (cross product > 0 for CCW)
        if (cross(ax, ay, bx, by, cx, cy) <= 0f) return false

        // No other vertex should be inside triangle (a, b, c)
        for (idx in indices) {
            if (idx == prevIdx || idx == currIdx || idx == nextIdx) continue
            val px = verts[idx * 2]
            val py = verts[idx * 2 + 1]
            if (pointInTriangle(px, py, ax, ay, bx, by, cx, cy)) return false
        }

        return true
    }

    // 2D cross product of vectors (a→b) and (a→c). Positive = CCW turn. */
    private fun cross(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Float {
        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)
    }

    // Returns true if point (px, py) is strictly inside triangle (ax,ay)-(bx,by)-(cx,cy).
    // Uses barycentric / cross-product method.
    private fun pointInTriangle(
        px: Float, py: Float,
        ax: Float, ay: Float,
        bx: Float, by: Float,
        cx: Float, cy: Float
    ): Boolean {
        val d1 = cross(ax, ay, bx, by, px, py)
        val d2 = cross(bx, by, cx, cy, px, py)
        val d3 = cross(cx, cy, ax, ay, px, py)
        val hasNeg = (d1 < 0f) || (d2 < 0f) || (d3 < 0f)
        val hasPos = (d1 > 0f) || (d2 > 0f) || (d3 > 0f)
        return !(hasNeg && hasPos)
    }

    private fun isPointInsidePolygon(glX: Float, glY: Float): Boolean {
        // triangulatedVertices is a flat array [x0,y0, x1,y1, x2,y2, x3,y3 ...]
        // each group of 6 floats = one triangle
        var i = 0
        while (i < triangulatedVertices.size - 5) {
            val ax = triangulatedVertices[i];     val ay = triangulatedVertices[i + 1]
            val bx = triangulatedVertices[i + 2]; val by = triangulatedVertices[i + 3]
            val cx = triangulatedVertices[i + 4]; val cy = triangulatedVertices[i + 5]

            if (pointInTriangle(glX, glY, ax, ay, bx, by, cx, cy)) return true
            i += 6
        }
        return false
    }

    private fun MutableList<Float>.addTriangle(
        verts: FloatArray,
        i0: Int, i1: Int, i2: Int
    ) {
        add(verts[i0 * 2]); add(verts[i0 * 2 + 1])
        add(verts[i1 * 2]); add(verts[i1 * 2 + 1])
        add(verts[i2 * 2]); add(verts[i2 * 2 + 1])
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer =
        ByteBuffer
            .allocateDirect(data.size * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(data)
                position(0)
            }

    private fun compileShader(type: Int, shaderCode: String): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, shaderCode)
        glCompileShader(shader)

        val compileStatus = IntArray(1)
        glGetShaderiv(shader, GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = glGetShaderInfoLog(shader)
            glDeleteShader(shader)
            throw RuntimeException("Shader compile failed: $error")
        }

        return shader
    }

    private var selectedVertexIndex: Int? = null
    private var isPainting = false

    fun handleTouch(x: Float, y: Float, width: Int, height: Int) {
        val (glX, glY) = screenToGL(x, y, width, height)

        if (selectedVertexIndex != null) {
            val idx = selectedVertexIndex!!
            vertices[idx] = glX
            vertices[idx + 1] = glY

            vertexBuffer.put(vertices)
            vertexBuffer.position(0)

            triangulatedVertices = triangulatePolygon(vertices)
            triangleBuffer = createFloatBuffer(triangulatedVertices)

            return
        }

        if (!isPainting) {
            for (i in vertices.indices step 2) {
                val vx = vertices[i]
                val vy = vertices[i + 1]
                val distance = sqrt((glX - vx).pow(2) + (glY - vy).pow(2))
                if (distance < TOUCH_RADIUS) {
                    selectedVertexIndex = i
                    break
                }
            }
        }

        if (isPointInsidePolygon(glX, glY) && selectedVertexIndex == null) {
            isPainting = true
            viewModel.addCircle(glX, glY)
        }
    }

    private fun screenToGL(x: Float, y: Float, width: Int, height: Int): Pair<Float, Float> {
        val glX = (x / width) * 2f - 1f
        val glY = -((y / height) * 2f - 1f)
        return glX to glY
    }

    fun resetTouchedVertexIndex() {
        selectedVertexIndex = null
        isPainting = false
    }

    companion object {
        private const val FLOAT_SIZE = 4
        private const val TOUCH_RADIUS = 0.1f
    }
}