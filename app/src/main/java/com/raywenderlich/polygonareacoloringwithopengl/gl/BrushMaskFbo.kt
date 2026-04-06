package com.raywenderlich.polygonareacoloringwithopengl.gl

import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BrushMaskFbo {

    var fboId: Int = 0
        private set
    var textureId: Int = 0
        private set

    private var savedPixels: ByteBuffer? = null
    private var pixelsSaved = false

    private val snapshots = ArrayDeque<ByteBuffer>()

    fun create(width: Int, height: Int) {
        release()

        val texIds = IntArray(1)
        glGenTextures(1, texIds, 0)
        textureId = texIds[0]

        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glBindTexture(GL_TEXTURE_2D, 0)

        val fboIds = IntArray(1)
        glGenFramebuffers(1, fboIds, 0)
        fboId = fboIds[0]

        glBindFramebuffer(GL_FRAMEBUFFER, fboId)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0)

        glClearColor(0f, 0f, 0f, 0f)
        glClear(GL_COLOR_BUFFER_BIT)

        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Brush mask FBO incomplete: $status")
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClearColor(0f, 0f, 0f, 1f)

        snapshots.clear()
        pushSnapshot(width, height)
    }

    fun release() {
        if (fboId != 0)     { glDeleteFramebuffers(1, intArrayOf(fboId), 0);      fboId = 0 }
        if (textureId != 0) { glDeleteTextures(1, intArrayOf(textureId), 0);       textureId = 0 }
    }

    fun savePixels(width: Int, height: Int) {
        if (fboId == 0 || width == 0 || height == 0) return
        val buffer = ByteBuffer
            .allocateDirect(width * height * 4)
            .order(ByteOrder.nativeOrder())
        glBindFramebuffer(GL_FRAMEBUFFER, fboId)
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        buffer.position(0)
        savedPixels = buffer
        pixelsSaved = true
    }

    fun restorePixelsIfNeeded(width: Int, height: Int) {
        if (!pixelsSaved) return
        val pixels = savedPixels ?: return
        pixels.position(0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
        glBindTexture(GL_TEXTURE_2D, 0)
        pixelsSaved = false
    }

    fun pushSnapshot(width: Int, height: Int) {
        if (fboId == 0 || width == 0 || height == 0) return
        val buffer = ByteBuffer
            .allocateDirect(width * height * 4)
            .order(ByteOrder.nativeOrder())
        glBindFramebuffer(GL_FRAMEBUFFER, fboId)
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        buffer.position(0)
        if (snapshots.size >= MAX_UNDO_STEPS) snapshots.removeFirst()
        snapshots.addLast(buffer)
    }

    fun undo(width: Int, height: Int) {
        if (snapshots.size <= 1) return
        snapshots.removeLast()
        val pixels = snapshots.last()
        pixels.position(0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    companion object {
        private const val MAX_UNDO_STEPS = 10
    }
}