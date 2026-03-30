//package com.raywenderlich.polygonareacoloringwithopengl
//
//import android.content.Context
//import android.opengl.GLES20.*
//import android.opengl.GLSurfaceView
//import android.view.MotionEvent
//import androidx.compose.ui.input.key.Key
//import java.nio.ByteBuffer
//import java.nio.FloatBuffer
//import javax.microedition.khronos.egl.EGLConfig
//import javax.microedition.khronos.opengles.GL10
//import com.raywenderlich.polygonareacoloringwithopengl.data.PolygonData
//import com.raywenderlich.polygonareacoloringwithopengl.data.PolygonData.vertices
//import com.raywenderlich.polygonareacoloringwithopengl.viewmodel.PolygonViewModel
//import java.nio.ByteOrder
//import kotlin.math.cos
//import kotlin.math.pow
//import kotlin.math.sin
//import kotlin.math.sqrt
//
//class PolygonRenderer(
//    private val context: Context,
//    private val viewModel: PolygonViewModel
//) : GLSurfaceView.Renderer {
//
//    private lateinit var vertexBuffer: FloatBuffer
//    private lateinit var triangleBuffer: FloatBuffer
//    private var positionHandle = 0
//    private var colorHandle = 0
//    private var program = 0
//    private var triangulatedVertices = FloatArray(0)
//
//    private val circleSegments = 40
//
//    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        glClearColor(0f, 0f, 0f, 1f)
//
//        vertexBuffer = ByteBuffer
//            .allocateDirect(vertices.size * FLOAT_SIZE)
//            .order(ByteOrder.nativeOrder())
//            .asFloatBuffer()
//        vertexBuffer.put(vertices)
//        vertexBuffer.position(0)
//
//        triangulatedVertices = triangulatePolygon(vertices)
//        triangleBuffer = createFloatBuffer(triangulatedVertices)
//
//        val vertexShaderCode = ShaderUtils.loadShaderFromAssets(context, "vertex_shader.glsl")
//        val fragmentShaderCode = ShaderUtils.loadShaderFromAssets(context, "fragment_shader.glsl")
//
//        val vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderCode)
//        val fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
//
//        program = glCreateProgram()
//        glAttachShader(program, vertexShader)
//        glAttachShader(program, fragmentShader)
//        glLinkProgram(program)
//
//        val linkStatus = IntArray(1)
//        glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)
//        if (linkStatus[0] == 0) {
//            val error = glGetProgramInfoLog(program)
//            glDeleteProgram(program)
//            throw RuntimeException("Program link failed: $error")
//        }
//
//        positionHandle = glGetAttribLocation(program, "aPosition")
//        colorHandle = glGetUniformLocation(program, "uColor")
//    }
//
//    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//        glViewport(0, 0, width, height)
//    }
//
//    override fun onDrawFrame(gl: GL10?) {
//        // Clear both color and stencil every frame
//        glClear(GL_COLOR_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
//
//        glUseProgram(program)
//
//        glEnable(GL_STENCIL_TEST)
//        glStencilFunc(GL_ALWAYS, 1, 0xFF)
//        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
//        glColorMask(false, false, false, false)
//
//        glEnableVertexAttribArray(positionHandle)
//        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, triangleBuffer)
//        glDrawArrays(GL_TRIANGLES, 0, triangulatedVertices.size / 2)
//        glDisableVertexAttribArray(positionHandle)
//
//        glStencilFunc(GL_EQUAL, 1, 0xFF)
//        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
//        glColorMask(true, true, true, true)
//
//        // draw polygon
////        glUseProgram(program)
//        glUniform4f(colorHandle, 1f, 0f, 0f, 1f)
//        glEnableVertexAttribArray(positionHandle)
//        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, triangleBuffer)
//        glDrawArrays(GL_TRIANGLES, 0, triangulatedVertices.size / 2)
//        glDisableVertexAttribArray(positionHandle)
//
//        val circles = viewModel.paintedCircles.value
//        for (circle in circles) {
//            if (circle.mode == ToolMode.BRUSH) {
//                drawCircle(circle.x, circle.y, circle.radius, 1f, 0f, 0f, 1f)
//            } else {
//                drawCircle(circle.x, circle.y, circle.radius, 1f, 1f, 1f, 1f)
//            }
//        }
//
//        glDisable(GL_STENCIL_TEST)
//
////        // draw line
////        glUniform4f(colorHandle, 1f, 1f, 0f, 1f) // yellow outline
////        glEnableVertexAttribArray(positionHandle)
////        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, vertexBuffer)
////        glDrawArrays(GL_LINE_LOOP, 0, vertices.size / 2)
////        glDisableVertexAttribArray(positionHandle)
//
//        // draw points
////        glUseProgram(program)
//        glUniform4f(colorHandle, 0f, 1f, 0f, 1f)
//        glEnableVertexAttribArray(positionHandle)
//        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, vertexBuffer)
//        glDrawArrays(GL_POINTS, 0, vertices.size / 2)
//        glDisableVertexAttribArray(positionHandle)
//    }
//
//    private fun drawCircle(
//        cx: Float, cy: Float, radius: Float,
//        r: Float, g: Float, b: Float, a: Float
//    ) {
//        val coords = FloatArray((circleSegments + 2) * 2)
//        // centre point
//        coords[0] = cx
//        coords[1] = cy
//        // surrounding points
//        for (i in 0..circleSegments) {
//            val angle = (2.0 * Math.PI * i / circleSegments).toFloat()
//            coords[(i + 1) * 2]     = cx + radius * cos(angle)
//            coords[(i + 1) * 2 + 1] = cy + radius * sin(angle)
//        }
//
//        val buffer = createFloatBuffer(coords)
//
//        glUniform4f(colorHandle, r, g, b, a)
//        glEnableVertexAttribArray(positionHandle)
//        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, buffer)
//        glDrawArrays(GL_TRIANGLE_FAN, 0, circleSegments + 2)
//        glDisableVertexAttribArray(positionHandle)
//    }
//
//    private fun triangulatePolygon(verts: FloatArray): FloatArray {
//        val n = verts.size / 2
//        if (n < 3) return FloatArray(0)
//
//        val indices = ArrayDeque<Int>((0 until n).toList())
//
//        if (!isCounterClockwise(verts, indices)) {
//            indices.reverse()
//        }
//
//        val result = mutableListOf<Float>()
//
//        var safetyCounter = 0
//        val maxIterations = n * n
//
//        while (indices.size > 3 && safetyCounter < maxIterations) {
//            var earFound = false
//            val size = indices.size
//
//            for (i in 0 until size) {
//                val prevIdx = indices[(i - 1 + size) % size]
//                val currIdx = indices[i]
//                val nextIdx = indices[(i + 1) % size]
//
//                if (isEar(verts, indices, prevIdx, currIdx, nextIdx)) {
//                    result.addTriangle(verts, prevIdx, currIdx, nextIdx)
//                    indices.removeAt(i)
//                    earFound = true
//                    break
//                }
//            }
//
//            if (!earFound) {
//                indices.removeAt(0)
//            }
//
//            safetyCounter++
//        }
//
//        if (indices.size == 3) {
//            result.addTriangle(verts, indices[0], indices[1], indices[2])
//        }
//
//        return result.toFloatArray()
//    }
//
//    // Returns true if the polygon vertices are wound counter-clockwise.
//    // Uses the shoelace formula to compute signed area.
//    private fun isCounterClockwise(verts: FloatArray, indices: ArrayDeque<Int>): Boolean {
//        var signedArea = 0f
//        val n = indices.size
//        for (i in 0 until n) {
//            val curr = indices[i]
//            val next = indices[(i + 1) % n]
//            val x0 = verts[curr * 2]
//            val y0 = verts[curr * 2 + 1]
//            val x1 = verts[next * 2]
//            val y1 = verts[next * 2 + 1]
//            signedArea += (x0 * y1) - (x1 * y0)
//        }
//        return signedArea > 0f
//    }
//
//    // Checks if vertex [curr] is an "ear" tip:
//    // 1. The triangle (prev, curr, next) must be convex (left turn in CCW polygon).
//    // 2. No other polygon vertex lies inside this triangle.
//    private fun isEar(
//        verts: FloatArray,
//        indices: ArrayDeque<Int>,
//        prevIdx: Int,
//        currIdx: Int,
//        nextIdx: Int
//    ): Boolean {
//        val ax = verts[prevIdx * 2];  val ay = verts[prevIdx * 2 + 1]
//        val bx = verts[currIdx * 2];  val by = verts[currIdx * 2 + 1]
//        val cx = verts[nextIdx * 2];  val cy = verts[nextIdx * 2 + 1]
//
//        // Must be a convex vertex (cross product > 0 for CCW)
//        if (cross(ax, ay, bx, by, cx, cy) <= 0f) return false
//
//        // No other vertex should be inside triangle (a, b, c)
//        for (idx in indices) {
//            if (idx == prevIdx || idx == currIdx || idx == nextIdx) continue
//            val px = verts[idx * 2]
//            val py = verts[idx * 2 + 1]
//            if (pointInTriangle(px, py, ax, ay, bx, by, cx, cy)) return false
//        }
//
//        return true
//    }
//
//    // 2D cross product of vectors (a→b) and (a→c). Positive = CCW turn. */
//    private fun cross(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Float {
//        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)
//    }
//
//    // Returns true if point (px, py) is strictly inside triangle (ax,ay)-(bx,by)-(cx,cy).
//    // Uses barycentric / cross-product method.
//    private fun pointInTriangle(
//        px: Float, py: Float,
//        ax: Float, ay: Float,
//        bx: Float, by: Float,
//        cx: Float, cy: Float
//    ): Boolean {
//        val d1 = cross(ax, ay, bx, by, px, py)
//        val d2 = cross(bx, by, cx, cy, px, py)
//        val d3 = cross(cx, cy, ax, ay, px, py)
//        val hasNeg = (d1 < 0f) || (d2 < 0f) || (d3 < 0f)
//        val hasPos = (d1 > 0f) || (d2 > 0f) || (d3 > 0f)
//        return !(hasNeg && hasPos)
//    }
//
//    private fun isPointInsidePolygon(glX: Float, glY: Float): Boolean {
//        // triangulatedVertices is a flat array [x0,y0, x1,y1, x2,y2, x3,y3 ...]
//        // each group of 6 floats = one triangle
//        var i = 0
//        while (i < triangulatedVertices.size - 5) {
//            val ax = triangulatedVertices[i];     val ay = triangulatedVertices[i + 1]
//            val bx = triangulatedVertices[i + 2]; val by = triangulatedVertices[i + 3]
//            val cx = triangulatedVertices[i + 4]; val cy = triangulatedVertices[i + 5]
//
//            if (pointInTriangle(glX, glY, ax, ay, bx, by, cx, cy)) return true
//            i += 6
//        }
//        return false
//    }
//
//    private fun MutableList<Float>.addTriangle(
//        verts: FloatArray,
//        i0: Int, i1: Int, i2: Int
//    ) {
//        add(verts[i0 * 2]); add(verts[i0 * 2 + 1])
//        add(verts[i1 * 2]); add(verts[i1 * 2 + 1])
//        add(verts[i2 * 2]); add(verts[i2 * 2 + 1])
//    }
//
//    private fun createFloatBuffer(data: FloatArray): FloatBuffer =
//        ByteBuffer
//            .allocateDirect(data.size * FLOAT_SIZE)
//            .order(ByteOrder.nativeOrder())
//            .asFloatBuffer()
//            .apply {
//                put(data)
//                position(0)
//            }
//
//    private fun compileShader(type: Int, shaderCode: String): Int {
//        val shader = glCreateShader(type)
//        glShaderSource(shader, shaderCode)
//        glCompileShader(shader)
//
//        val compileStatus = IntArray(1)
//        glGetShaderiv(shader, GL_COMPILE_STATUS, compileStatus, 0)
//        if (compileStatus[0] == 0) {
//            val error = glGetShaderInfoLog(shader)
//            glDeleteShader(shader)
//            throw RuntimeException("Shader compile failed: $error")
//        }
//
//        return shader
//    }
//
//    private var selectedVertexIndex: Int? = null
//    private var isPainting = false
//
//    fun handleTouch(x: Float, y: Float, width: Int, height: Int) {
//        val (glX, glY) = screenToGL(x, y, width, height)
//
//        if (selectedVertexIndex != null) {
//            val idx = selectedVertexIndex!!
//            vertices[idx] = glX
//            vertices[idx + 1] = glY
//
//            vertexBuffer.put(vertices)
//            vertexBuffer.position(0)
//
//            triangulatedVertices = triangulatePolygon(vertices)
//            triangleBuffer = createFloatBuffer(triangulatedVertices)
//
//            return
//        }
//
//        if (!isPainting) {
//            for (i in vertices.indices step 2) {
//                val vx = vertices[i]
//                val vy = vertices[i + 1]
//                val distance = sqrt((glX - vx).pow(2) + (glY - vy).pow(2))
//                if (distance < TOUCH_RADIUS) {
//                    selectedVertexIndex = i
//                    break
//                }
//            }
//        }
//
//        if (isPointInsidePolygon(glX, glY) && selectedVertexIndex == null) {
//            isPainting = true
//            viewModel.addCircle(glX, glY)
//        }
//    }
//
//    private fun screenToGL(x: Float, y: Float, width: Int, height: Int): Pair<Float, Float> {
//        val glX = (x / width) * 2f - 1f
//        val glY = -((y / height) * 2f - 1f)
//        return glX to glY
//    }
//
//    fun resetTouchedVertexIndex() {
//        selectedVertexIndex = null
//        isPainting = false
//    }
//
//    companion object {
//        private const val FLOAT_SIZE = 4
//        private const val TOUCH_RADIUS = 0.1f
//    }
//}


package com.raywenderlich.polygonareacoloringwithopengl

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.raywenderlich.polygonareacoloringwithopengl.data.PolygonData.vertices
import com.raywenderlich.polygonareacoloringwithopengl.viewmodel.PolygonViewModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class PolygonRenderer(
    private val context: Context,
    private val viewModel: PolygonViewModel
) : GLSurfaceView.Renderer {

    private var polygonProgram = 0
    private var polyPositionHandle = 0
    private var polyColorHandle = 0


    private var brushProgram = 0
    private var brushPositionHandle = 0
    private var brushTexCoordHandle = 0
    private var brushColorUniform = 0
    private var brushHardnessUniform = 0
    private var brushOpacityUniform = 0


    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var triangleBuffer: FloatBuffer
    private var triangulatedVertices = FloatArray(0)

    // FBO — brush mask
    // The brush and erase strokes are stamped into this offscreen texture.
    // Its alpha channel IS the paint mask.
    private var brushMaskFbo = 0   // framebuffer object id
    private var brushMaskTexture = 0   // RGBA texture attached to the FBO
    private var surfaceWidth = 1
    private var surfaceHeight = 1

    // Full-screen quad used to blit the brush mask onto the screen
    // positions (xy) + uvs (st) interleaved: x, y, u, v
    private val fullScreenQuadData = floatArrayOf(
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f,
        -1f, 1f, 0f, 1f,
        1f, 1f, 1f, 1f
    )
    private lateinit var fullScreenQuadBuffer: FloatBuffer

    // Texture sampler handle for compositing the mask to screen
    private var maskSamplerUniform = 0

    // Second simple program to blit a texture to the screen
    private var blitProgram = 0
    private var blitPositionHandle = 0
    private var blitTexCoordHandle = 0
    private var blitSamplerHandle = 0

    private var selectedVertexIndex: Int? = null
    private var isPainting = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 1f)

        vertexBuffer = createFloatBuffer(vertices)

        triangulatedVertices = triangulatePolygon(vertices)
        triangleBuffer = createFloatBuffer(triangulatedVertices)

        polygonProgram = buildProgram(
            ShaderUtils.loadShaderFromAssets(context, "vertex_shader.glsl"),
            ShaderUtils.loadShaderFromAssets(context, "fragment_shader.glsl")
        )
        polyPositionHandle = glGetAttribLocation(polygonProgram, "aPosition")
        polyColorHandle = glGetUniformLocation(polygonProgram, "uColor")

        brushProgram = buildProgram(
            ShaderUtils.loadShaderFromAssets(context, "brush_vertex_shader.glsl"),
            ShaderUtils.loadShaderFromAssets(context, "brush_fragment_shader.glsl")
        )
        brushPositionHandle = glGetAttribLocation(brushProgram, "aPosition")
        brushTexCoordHandle = glGetAttribLocation(brushProgram, "aTexCoord")
        brushColorUniform = glGetUniformLocation(brushProgram, "u_color")
        brushHardnessUniform = glGetUniformLocation(brushProgram, "u_hardness")
        brushOpacityUniform = glGetUniformLocation(brushProgram, "u_opacity")

        // Blit program — draws the brush mask texture to the screen.
        // Simple pass-through: samples the texture and outputs the color.
        blitProgram = buildProgram(BLIT_VERTEX_SRC, BLIT_FRAGMENT_SRC)
        blitPositionHandle = glGetAttribLocation(blitProgram, "aPosition")
        blitTexCoordHandle = glGetAttribLocation(blitProgram, "aTexCoord")
        blitSamplerHandle = glGetUniformLocation(blitProgram, "uTexture")

        // full-screen quad buffer (positions + uvs interleaved)
        fullScreenQuadBuffer = createFloatBuffer(fullScreenQuadData)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        surfaceWidth = width
        surfaceHeight = height
        createBrushMaskFbo(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)           // default framebuffer
        glClear(GL_COLOR_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

        // Pass 1: stencil mask
        glUseProgram(polygonProgram)
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_ALWAYS, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
        glColorMask(false, false, false, false)        // don't write color

        glEnableVertexAttribArray(polyPositionHandle)
        glVertexAttribPointer(polyPositionHandle, 2, GL_FLOAT, false, 0, triangleBuffer)
        glDrawArrays(GL_TRIANGLES, 0, triangulatedVertices.size / 2)
        glDisableVertexAttribArray(polyPositionHandle)

        // Pass 2a: polygon fill (stencil = 1 only)
        glStencilFunc(GL_EQUAL, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glColorMask(true, true, true, true)

        glUniform4f(polyColorHandle, 0.9f, 0.9f, 0.9f, 1f)  // light-grey base
//        glUniform4f(polyColorHandle, 1f, 0f, 0f, 1f)
        glEnableVertexAttribArray(polyPositionHandle)
        glVertexAttribPointer(polyPositionHandle, 2, GL_FLOAT, false, 0, triangleBuffer)
        glDrawArrays(GL_TRIANGLES, 0, triangulatedVertices.size / 2)
        glDisableVertexAttribArray(polyPositionHandle)

        // Pass 2b: blit brush-mask texture (still stencil-clipped)
        // Enable blending so the painted alpha from the mask shows through.
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glUseProgram(blitProgram)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, brushMaskTexture)
        glUniform1i(blitSamplerHandle, 0)

        // stride = 4 floats per vertex (x, y, u, v), each 4 bytes = 16 bytes
        val stride = 4 * FLOAT_SIZE
        fullScreenQuadBuffer.position(0)
        glEnableVertexAttribArray(blitPositionHandle)
        glVertexAttribPointer(blitPositionHandle, 2, GL_FLOAT, false, stride, fullScreenQuadBuffer)

        fullScreenQuadBuffer.position(2)               // uv starts at offset 2
        glEnableVertexAttribArray(blitTexCoordHandle)
        glVertexAttribPointer(blitTexCoordHandle, 2, GL_FLOAT, false, stride, fullScreenQuadBuffer)

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

        glDisableVertexAttribArray(blitPositionHandle)
        glDisableVertexAttribArray(blitTexCoordHandle)
        glDisable(GL_BLEND)

        // Pass 3: vertex points (no stencil)
        glDisable(GL_STENCIL_TEST)

        glUseProgram(polygonProgram)
        glUniform4f(polyColorHandle, 0f, 1f, 0f, 1f)  // green dots
        glEnableVertexAttribArray(polyPositionHandle)
        glVertexAttribPointer(polyPositionHandle, 2, GL_FLOAT, false, 0, vertexBuffer)
        glDrawArrays(GL_POINTS, 0, vertices.size / 2)
        glDisableVertexAttribArray(polyPositionHandle)
    }

    // stampBrush — called from the GL thread via queueEvent()
    // Binds the brushMaskFBO, sets the correct blend equation for the current
    // tool mode, then draws a single soft quad stamp at (glX, glY).
    // BRUSH mode blend:
    //   glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
    //   → adds paint with soft alpha accumulation
    //
    // ERASE mode blend:
    //   glBlendFuncSeparate(ZERO, ONE, ZERO, ONE_MINUS_SRC_ALPHA)
    //   → leaves RGB untouched, subtracts alpha from mask
    private fun stampBrush(glX: Float, glY: Float) {
        val r = viewModel.brushRadius

        // Build a quad (2 triangles as TRIANGLE_STRIP) around the touch point.
        // Each vertex: x, y, u, v
        val quadData = floatArrayOf(
            glX - r, glY - r, 0f, 0f,
            glX + r, glY - r, 1f, 0f,
            glX - r, glY + r, 0f, 1f,
            glX + r, glY + r, 1f, 1f
        )
        val quadBuffer = createFloatBuffer(quadData)

        // Bind the brush mask FBO
        glBindFramebuffer(GL_FRAMEBUFFER, brushMaskFbo)
        glViewport(0, 0, surfaceWidth, surfaceHeight)

        glUseProgram(brushProgram)

        // Set blend equation based on tool mode
        glEnable(GL_BLEND)
        if (viewModel.toolMode.value == ToolMode.ERASE) {
            // Erase: subtract alpha from the mask.
            // RGB component: ZERO * src + ONE * dst  → dst RGB unchanged
            // Alpha component: ZERO * src.a + (1 - src.a) * dst.a  → alpha reduced
            glBlendFuncSeparate(
                GL_ZERO,                    // src RGB factor
                GL_ONE,                     // dst RGB factor
                GL_ZERO,                    // src Alpha factor
                GL_ONE_MINUS_SRC_ALPHA      // dst Alpha factor
            )
        } else {
            // Brush: paint color with soft alpha onto the mask.
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }

        // Upload brush uniforms
        val color = viewModel.brushColor
        glUniform4f(brushColorUniform, color[0], color[1], color[2], color[3])
        glUniform1f(brushHardnessUniform, viewModel.brushHardness)
        glUniform1f(brushOpacityUniform, viewModel.brushOpacity)

        // Draw the stamp quad
        val stride = 4 * FLOAT_SIZE
        quadBuffer.position(0)
        glEnableVertexAttribArray(brushPositionHandle)
        glVertexAttribPointer(brushPositionHandle, 2, GL_FLOAT, false, stride, quadBuffer)

        quadBuffer.position(2)
        glEnableVertexAttribArray(brushTexCoordHandle)
        glVertexAttribPointer(brushTexCoordHandle, 2, GL_FLOAT, false, stride, quadBuffer)

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

        glDisableVertexAttribArray(brushPositionHandle)
        glDisableVertexAttribArray(brushTexCoordHandle)
        glDisable(GL_BLEND)

        // Restore default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(0, 0, surfaceWidth, surfaceHeight)
    }

    // createBrushMaskFbo — (re)creates the offscreen framebuffer + texture
    // Called on surface creation and on every resize.
    private fun createBrushMaskFbo(width: Int, height: Int) {
        if (brushMaskFbo != 0) {
            glDeleteFramebuffers(1, intArrayOf(brushMaskFbo), 0); brushMaskFbo = 0
        }
        if (brushMaskTexture != 0) {
            glDeleteTextures(1, intArrayOf(brushMaskTexture), 0); brushMaskTexture = 0
        }

        // Create texture
        val texIds = IntArray(1)
        glGenTextures(1, texIds, 0)
        brushMaskTexture = texIds[0]

        glBindTexture(GL_TEXTURE_2D, brushMaskTexture)
        glTexImage2D(
            GL_TEXTURE_2D, 0, GL_RGBA,
            width, height, 0,
            GL_RGBA, GL_UNSIGNED_BYTE, null     // null = empty / transparent
        )
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        // Create FBO and attach texture
        val fboIds = IntArray(1)
        glGenFramebuffers(1, fboIds, 0)
        brushMaskFbo = fboIds[0]

        glBindFramebuffer(GL_FRAMEBUFFER, brushMaskFbo)
        glFramebufferTexture2D(
            GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D, brushMaskTexture, 0
        )

        // Clear the mask to fully transparent
        glClearColor(0f, 0f, 0f, 0f)
        glClear(GL_COLOR_BUFFER_BIT)

        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Brush mask FBO incomplete: $status")
        }

        // Restore default framebuffer and clear color
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClearColor(0f, 0f, 0f, 1f)
    }

    private var glSurfaceView: GLSurfaceView? = null

    fun attachSurface(view: GLSurfaceView) {
        glSurfaceView = view
    }

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
                val dist = sqrt((glX - vertices[i]).pow(2) + (glY - vertices[i + 1]).pow(2))
                if (dist < TOUCH_RADIUS) {
                    selectedVertexIndex = i
                    return
                }
            }
        }

        if (isPointInsidePolygon(glX, glY) && selectedVertexIndex == null) {
            isPainting = true
            glSurfaceView?.queueEvent { stampBrush(glX, glY) }
        }
    }

    fun resetTouchedVertexIndex() {
        selectedVertexIndex = null
        isPainting = false
    }

    private fun screenToGL(x: Float, y: Float, width: Int, height: Int): Pair<Float, Float> {
        val glX = (x / width) * 2f - 1f
        val glY = -((y / height) * 2f - 1f)
        return glX to glY
    }

    private fun buildProgram(vertSrc: String, fragSrc: String): Int {
        val vert = compileShader(GL_VERTEX_SHADER, vertSrc)
        val frag = compileShader(GL_FRAGMENT_SHADER, fragSrc)
        val prog = glCreateProgram()
        glAttachShader(prog, vert)
        glAttachShader(prog, frag)
        glLinkProgram(prog)

        val status = IntArray(1)
        glGetProgramiv(prog, GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            val err = glGetProgramInfoLog(prog)
            glDeleteProgram(prog)
            throw RuntimeException("Program link failed: $err")
        }
        return prog
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, src)
        glCompileShader(shader)

        val status = IntArray(1)
        glGetShaderiv(shader, GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val err = glGetShaderInfoLog(shader)
            glDeleteShader(shader)
            throw RuntimeException("Shader compile failed: $err")
        }
        return shader
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer =
        ByteBuffer
            .allocateDirect(data.size * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(data); position(0) }

    private fun triangulatePolygon(verts: FloatArray): FloatArray {
        val n = verts.size / 2
        if (n < 3) return FloatArray(0)
        val indices = ArrayDeque<Int>((0 until n).toList())
        if (!isCounterClockwise(verts, indices)) indices.reverse()

        val result = mutableListOf<Float>()
        var safety = 0
        val maxIter = n * n

        while (indices.size > 3 && safety < maxIter) {
            var earFound = false
            val size = indices.size
            for (i in 0 until size) {
                val prev = indices[(i - 1 + size) % size]
                val curr = indices[i]
                val next = indices[(i + 1) % size]
                if (isEar(verts, indices, prev, curr, next)) {
                    result.addTriangle(verts, prev, curr, next)
                    indices.removeAt(i)
                    earFound = true
                    break
                }
            }
            if (!earFound) indices.removeAt(0)
            safety++
        }
        if (indices.size == 3) result.addTriangle(verts, indices[0], indices[1], indices[2])
        return result.toFloatArray()
    }

    private fun isCounterClockwise(verts: FloatArray, indices: ArrayDeque<Int>): Boolean {
        var area = 0f
        val n = indices.size
        for (i in 0 until n) {
            val c = indices[i];
            val nx = indices[(i + 1) % n]
            area += verts[c * 2] * verts[nx * 2 + 1] - verts[nx * 2] * verts[c * 2 + 1]
        }
        return area > 0f
    }

    private fun isEar(
        verts: FloatArray,
        indices: ArrayDeque<Int>,
        p: Int,
        c: Int,
        n: Int
    ): Boolean {
        val ax = verts[p * 2];
        val ay = verts[p * 2 + 1]
        val bx = verts[c * 2];
        val by = verts[c * 2 + 1]
        val cx = verts[n * 2];
        val cy = verts[n * 2 + 1]
        if (cross(ax, ay, bx, by, cx, cy) <= 0f) return false
        for (idx in indices) {
            if (idx == p || idx == c || idx == n) continue
            if (pointInTriangle(
                    verts[idx * 2],
                    verts[idx * 2 + 1],
                    ax,
                    ay,
                    bx,
                    by,
                    cx,
                    cy
                )
            ) return false
        }
        return true
    }

    private fun cross(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float) =
        (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)

    private fun pointInTriangle(
        px: Float, py: Float,
        ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float
    ): Boolean {
        val d1 = cross(ax, ay, bx, by, px, py)
        val d2 = cross(bx, by, cx, cy, px, py)
        val d3 = cross(cx, cy, ax, ay, px, py)
        return !((d1 < 0f || d2 < 0f || d3 < 0f) && (d1 > 0f || d2 > 0f || d3 > 0f))
    }

    private fun isPointInsidePolygon(glX: Float, glY: Float): Boolean {
        var i = 0
        while (i < triangulatedVertices.size - 5) {
            if (pointInTriangle(
                    glX, glY,
                    triangulatedVertices[i], triangulatedVertices[i + 1],
                    triangulatedVertices[i + 2], triangulatedVertices[i + 3],
                    triangulatedVertices[i + 4], triangulatedVertices[i + 5]
                )
            ) return true
            i += 6
        }
        return false
    }

    private fun MutableList<Float>.addTriangle(verts: FloatArray, i0: Int, i1: Int, i2: Int) {
        add(verts[i0 * 2]); add(verts[i0 * 2 + 1])
        add(verts[i1 * 2]); add(verts[i1 * 2 + 1])
        add(verts[i2 * 2]); add(verts[i2 * 2 + 1])
    }

    companion object {
        private const val FLOAT_SIZE = 4
        private const val TOUCH_RADIUS = 0.1f

        private const val BLIT_VERTEX_SRC = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord   = aTexCoord;
            }"""

        private const val BLIT_FRAGMENT_SRC = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }"""
    }
}