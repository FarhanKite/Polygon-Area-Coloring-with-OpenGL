package com.raywenderlich.polygonareacoloringwithopengl

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import com.raywenderlich.polygonareacoloringwithopengl.data.PolygonData
import com.raywenderlich.polygonareacoloringwithopengl.data.PolygonData.vertices
import java.nio.ByteOrder
import kotlin.math.pow
import kotlin.math.sqrt

class PolygonRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var vertexBuffer: FloatBuffer
    private var positionHandle = 0
    private var colorHandle = 0
    private var program = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 1f)

        vertexBuffer = ByteBuffer
            .allocateDirect(vertices.size * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

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
        glClear(GL_COLOR_BUFFER_BIT)

        // draw points
        glUseProgram(program)
        glUniform4f(colorHandle, 1f, 0f, 0f, 1f)
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, vertexBuffer)
        glDrawArrays(GL_TRIANGLE_FAN, 0, vertices.size / 2)
        glDisableVertexAttribArray(positionHandle)

        // draw points
        glUseProgram(program)
        glUniform4f(colorHandle, 1f, 1f, 1f, 1f)
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, 0, vertexBuffer)
        glDrawArrays(GL_POINTS, 0, vertices.size / 2)
        glDisableVertexAttribArray(positionHandle)
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

    fun handleTouch(x: Float, y: Float, width: Int, height: Int) {
        val (glX, glY) = screenToGL(x, y, width, height)

        if (selectedVertexIndex == null) {
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

        selectedVertexIndex?.let {
            vertices[it] = glX
            vertices[it + 1] = glY

            vertexBuffer.put(vertices)
            vertexBuffer.position(0)
        }
    }

    private fun screenToGL(x: Float, y: Float, width: Int, height: Int): Pair<Float, Float> {
        val glX = (x / width) * 2f - 1f
        val glY = -((y / height) * 2f - 1f)
        return glX to glY
    }

    fun resetTouchedVertexIndex() {
        selectedVertexIndex = null
    }

    companion object {
        private const val FLOAT_SIZE = 4
        private const val TOUCH_RADIUS = 0.1f
    }
}