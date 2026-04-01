package com.raywenderlich.polygonareacoloringwithopengl.gl

import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object GlBufferUtils {

    private const val FLOAT_SIZE = 4

    fun createFloatBuffer(data: FloatArray): FloatBuffer =
        ByteBuffer
            .allocateDirect(data.size * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(data); position(0) }

    fun compileShader(type: Int, src: String): Int {
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

    fun buildProgram(vertSrc: String, fragSrc: String): Int {
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
}