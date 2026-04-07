package com.raywenderlich.polygonareacoloringwithopengl.gl

import android.opengl.GLES20.*
import com.raywenderlich.polygonareacoloringwithopengl.ToolMode
import com.raywenderlich.polygonareacoloringwithopengl.viewmodel.PolygonViewModel
import kotlin.math.*

class BrushStamper(
    private val positionHandle: Int,
    private val texCoordHandle: Int,
    private val colorUniform: Int,
    private val opacityUniform: Int,
    private val centerPointUniform: Int,
    private val brushRadiusUniform: Int,
    private val resolutionUniform: Int,
    private val programId: Int
) {
    private val floatSize = 4

    fun stamp(
        prevGlX: Float,
        prevGlY: Float,
        glX: Float,
        glY: Float,
        viewModel: PolygonViewModel,
        fbo: BrushMaskFbo,
        surfaceWidth: Int,
        surfaceHeight: Int
    ) {
        val glRadius = viewModel.brushRadiusPx / surfaceWidth

        val dx = glX - prevGlX
        val dy = glY - prevGlY
        val segmentLength = sqrt(dx * dx + dy * dy)

        val stepSize = glRadius * 0.18f
        val steps = ((segmentLength / stepSize).toInt()).coerceAtLeast(1)

        glBindFramebuffer(GL_FRAMEBUFFER, fbo.fboId)
        glViewport(0, 0, surfaceWidth, surfaceHeight)
        glDisable(GL_STENCIL_TEST)
        glUseProgram(programId)
        glEnable(GL_BLEND)

        if (viewModel.toolMode.value == ToolMode.ERASE) {
            glBlendFuncSeparate(
                GL_ZERO, GL_ONE_MINUS_SRC_ALPHA,
                GL_ZERO, GL_ONE_MINUS_SRC_ALPHA
            )
        } else {
            glBlendFuncSeparate(
                GL_ONE, GL_ONE_MINUS_SRC_ALPHA,
                GL_ONE, GL_ONE_MINUS_SRC_ALPHA
            )
        }

        val color = viewModel.brushColor
        glUniform4f(colorUniform, color[0], color[1], color[2], color[3])
        glUniform1f(opacityUniform, viewModel.brushOpacity)
        glUniform1f(brushRadiusUniform, glRadius)
        glUniform2f(resolutionUniform, surfaceWidth.toFloat(), surfaceHeight.toFloat())

        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val cx = prevGlX + dx * t
            val cy = prevGlY + dy * t

            val quadData = floatArrayOf(
                cx - glRadius, cy - glRadius, 0f, 0f,
                cx + glRadius, cy - glRadius, 1f, 0f,
                cx - glRadius, cy + glRadius, 0f, 1f,
                cx + glRadius, cy + glRadius, 1f, 1f
            )
            val quadBuffer = GlBufferUtils.createFloatBuffer(quadData)

            glUniform2f(centerPointUniform, cx, cy)

            val stride = 4 * floatSize
            quadBuffer.position(0)
            glEnableVertexAttribArray(positionHandle)
            glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, stride, quadBuffer)

            quadBuffer.position(2)
            glEnableVertexAttribArray(texCoordHandle)
            glVertexAttribPointer(texCoordHandle, 2, GL_FLOAT, false, stride, quadBuffer)

            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        }

        glDisableVertexAttribArray(positionHandle)
        glDisableVertexAttribArray(texCoordHandle)
        glDisable(GL_BLEND)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(0, 0, surfaceWidth, surfaceHeight)
    }
}
