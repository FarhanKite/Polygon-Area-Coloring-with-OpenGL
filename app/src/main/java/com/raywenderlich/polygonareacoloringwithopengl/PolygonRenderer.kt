package com.raywenderlich.polygonareacoloringwithopengl

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.raywenderlich.polygonareacoloringwithopengl.data.PolygonData.vertices
import com.raywenderlich.polygonareacoloringwithopengl.geometry.PolygonTriangulator
import com.raywenderlich.polygonareacoloringwithopengl.gl.BrushMaskFbo
import com.raywenderlich.polygonareacoloringwithopengl.gl.BrushStamper
import com.raywenderlich.polygonareacoloringwithopengl.gl.GlBufferUtils
import com.raywenderlich.polygonareacoloringwithopengl.input.TouchHandler
import com.raywenderlich.polygonareacoloringwithopengl.viewmodel.PolygonViewModel
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PolygonRenderer(
    private val context: Context,
    private val viewModel: PolygonViewModel
) : GLSurfaceView.Renderer {

    private var polygonProgram = 0
    private var polyPositionHandle = 0
    private var polyColorHandle = 0

    private var blitProgram = 0
    private var blitPositionHandle = 0
    private var blitTexCoordHandle = 0
    private var blitSamplerHandle = 0

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var triangleBuffer: FloatBuffer
    private var triangulatedVertices = FloatArray(0)
    private lateinit var fullScreenQuadBuffer: FloatBuffer

    private val brushMaskFbo = BrushMaskFbo()
    private lateinit var brushStamper: BrushStamper
    private lateinit var touchHandler: TouchHandler

    @Volatile private var surfaceWidth = 1
    @Volatile private var surfaceHeight = 1

    private var glSurfaceView: GLSurfaceView? = null

    fun attachSurface(view: GLSurfaceView) {
        glSurfaceView = view
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 1f)

        vertexBuffer = GlBufferUtils.createFloatBuffer(vertices)
        triangulatedVertices = PolygonTriangulator.triangulate(vertices)
        triangleBuffer = GlBufferUtils.createFloatBuffer(triangulatedVertices)
        fullScreenQuadBuffer = GlBufferUtils.createFloatBuffer(FULLSCREEN_QUAD_DATA)

        setupPolygonProgram()
        setupBrushProgram()
        setupBlitProgram()

        touchHandler = TouchHandler(
            onVertexMoved = { newTriangulation ->
                triangulatedVertices = newTriangulation
                vertexBuffer.put(vertices).position(0)
                triangleBuffer = GlBufferUtils.createFloatBuffer(triangulatedVertices)
            },
            onPaintRequest = { prevGlX, prevGlY, glX, glY ->
                glSurfaceView?.queueEvent {
                    // Convert pixel radius to GL space (width-based)
                    val glRadius = viewModel.brushRadiusPx / surfaceWidth
                    brushStamper.stamp(prevGlX, prevGlY, glX, glY, viewModel, brushMaskFbo, surfaceWidth, surfaceHeight)
                }
            },
            onStrokeEnded = {
                // Capture on calling thread (UI thread) before dispatching to GL thread
                val w = surfaceWidth
                val h = surfaceHeight
                glSurfaceView?.queueEvent {
                    brushMaskFbo.pushSnapshot(w, h)
                }
            },
            viewModel
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        surfaceWidth = width
        surfaceHeight = height
        brushMaskFbo.create(width, height)
        brushMaskFbo.restorePixelsIfNeeded(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        drawBackground()
        drawMask()
        drawVertexPoints()
    }

    fun handleTouch(prevX: Float, prevY: Float, x: Float, y: Float, width: Int, height: Int) {
        touchHandler.onTouch(prevX, prevY, x, y, width, height, triangulatedVertices)
    }

    fun resetTouchedVertexIndex() {
        touchHandler.onTouchUp()
    }

    fun saveMaskPixels() {
        brushMaskFbo.savePixels(surfaceWidth, surfaceHeight)
    }

    fun undo() {
        brushMaskFbo.undo(surfaceWidth, surfaceHeight)
    }

    private fun drawBackground() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClear(GL_COLOR_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

        glUseProgram(polygonProgram)

        // Pass 1: write stencil for the polygon area
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_ALWAYS, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
        glColorMask(false, false, false, false)

        glEnableVertexAttribArray(polyPositionHandle)
        glVertexAttribPointer(polyPositionHandle, 2, GL_FLOAT, false, 0, triangleBuffer)
        glDrawArrays(GL_TRIANGLES, 0, triangulatedVertices.size / 2)
        glDisableVertexAttribArray(polyPositionHandle)

        // Pass 2: draw polygon fill only where stencil == 1
        glStencilFunc(GL_EQUAL, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glColorMask(true, true, true, true)

        glUniform4f(polyColorHandle, 0.9f, 0.9f, 0.9f, 1f)
        glEnableVertexAttribArray(polyPositionHandle)
        glVertexAttribPointer(polyPositionHandle, 2, GL_FLOAT, false, 0, triangleBuffer)
        glDrawArrays(GL_TRIANGLES, 0, triangulatedVertices.size / 2)
        glDisableVertexAttribArray(polyPositionHandle)
    }

    private fun drawMask() {
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_EQUAL, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)

        glEnable(GL_BLEND)
        // Premultiplied blit — correctly composites FBO texture over background
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)

        glUseProgram(blitProgram)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, brushMaskFbo.textureId)
        glUniform1i(blitSamplerHandle, 0)

        val stride = 4 * FLOAT_SIZE
        fullScreenQuadBuffer.position(0)
        glEnableVertexAttribArray(blitPositionHandle)
        glVertexAttribPointer(blitPositionHandle, 2, GL_FLOAT, false, stride, fullScreenQuadBuffer)

        fullScreenQuadBuffer.position(2)
        glEnableVertexAttribArray(blitTexCoordHandle)
        glVertexAttribPointer(blitTexCoordHandle, 2, GL_FLOAT, false, stride, fullScreenQuadBuffer)

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

        glDisableVertexAttribArray(blitPositionHandle)
        glDisableVertexAttribArray(blitTexCoordHandle)
        glDisable(GL_BLEND)
        glDisable(GL_STENCIL_TEST)
    }

    private fun drawVertexPoints() {
        glUseProgram(polygonProgram)
        glUniform4f(polyColorHandle, 0f, 1f, 0f, 1f)
        glEnableVertexAttribArray(polyPositionHandle)
        glVertexAttribPointer(polyPositionHandle, 2, GL_FLOAT, false, 0, vertexBuffer)
        glDrawArrays(GL_POINTS, 0, vertices.size / 2)
        glDisableVertexAttribArray(polyPositionHandle)
    }

    private fun setupPolygonProgram() {
        polygonProgram = GlBufferUtils.buildProgram(
            ShaderUtils.loadShaderFromAssets(context, "vertex_shader.glsl"),
            ShaderUtils.loadShaderFromAssets(context, "fragment_shader.glsl")
        )
        polyPositionHandle = glGetAttribLocation(polygonProgram, "aPosition")
        polyColorHandle = glGetUniformLocation(polygonProgram, "uColor")
    }

    private fun setupBrushProgram() {
        val brushProgram = GlBufferUtils.buildProgram(
            ShaderUtils.loadShaderFromAssets(context, "brush_vertex_shader.glsl"),
            ShaderUtils.loadShaderFromAssets(context, "brush_fragment_shader.glsl")
        )
        brushStamper = BrushStamper(
            positionHandle     = glGetAttribLocation(brushProgram, "aPosition"),
            texCoordHandle     = glGetAttribLocation(brushProgram, "aTexCoord"),
            colorUniform       = glGetUniformLocation(brushProgram, "u_color"),
            opacityUniform     = glGetUniformLocation(brushProgram, "u_opacity"),
            centerPointUniform = glGetUniformLocation(brushProgram, "u_centerPoint"),
            brushRadiusUniform = glGetUniformLocation(brushProgram, "u_brushRadius"),
            resolutionUniform  = glGetUniformLocation(brushProgram, "u_resolution"), // ← add this
            programId          = brushProgram
        )
    }

    private fun setupBlitProgram() {
        blitProgram = GlBufferUtils.buildProgram(
            ShaderUtils.loadShaderFromAssets(context, "blit_vertex_shader.glsl"),
            ShaderUtils.loadShaderFromAssets(context, "blit_fragment_shader.glsl")
        )
        blitPositionHandle = glGetAttribLocation(blitProgram, "aPosition")
        blitTexCoordHandle = glGetAttribLocation(blitProgram, "aTexCoord")
        blitSamplerHandle  = glGetUniformLocation(blitProgram, "uTexture")
    }

    companion object {
        private const val FLOAT_SIZE = 4

        private val FULLSCREEN_QUAD_DATA = floatArrayOf(
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f,
            -1f,  1f, 0f, 1f,
            1f,  1f, 1f, 1f
        )
    }
}