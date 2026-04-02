package com.raywenderlich.polygonareacoloringwithopengl.gl

import android.opengl.GLES20.*
import com.raywenderlich.polygonareacoloringwithopengl.ToolMode
import com.raywenderlich.polygonareacoloringwithopengl.viewmodel.PolygonViewModel

class BrushStamper(
    private val positionHandle: Int,
    private val texCoordHandle: Int,
    private val colorUniform: Int,
    private val hardnessUniform: Int,
    private val opacityUniform: Int,
    private val prevPointUniform: Int,
    private val curPointUniform: Int,
    private val brushRadius: Int,
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
        val quadData = floatArrayOf(
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f,
            -1f,  1f, 0f, 1f,
            1f,  1f, 1f, 1f
        )
        val quadBuffer = GlBufferUtils.createFloatBuffer(quadData)

        glBindFramebuffer(GL_FRAMEBUFFER, fbo.fboId)
        glViewport(0, 0, surfaceWidth, surfaceHeight)

        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_EQUAL, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)

        glUseProgram(programId)
        glEnable(GL_BLEND)

        val maskUniform = glGetUniformLocation(programId, "u_mask")
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, fbo.textureId)  // read current FBO texture
        glUniform1i(maskUniform, 0)

        val color = viewModel.brushColor
        glUniform4f(colorUniform, color[0], color[1], color[2], color[3])
        glUniform1f(hardnessUniform, viewModel.brushHardness)
        glUniform1f(opacityUniform, viewModel.brushOpacity)
        glUniform2f(prevPointUniform, prevGlX, prevGlY)
        glUniform2f(curPointUniform, glX, glY)
        glUniform1f(brushRadius, viewModel.brushRadius)

        if (viewModel.toolMode.value == ToolMode.ERASE) {
            glBlendFuncSeparate(GL_ZERO, GL_ONE, GL_ZERO, GL_ONE_MINUS_SRC_ALPHA)
        } else {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }

        val stride = 4 * floatSize
        quadBuffer.position(0)
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, 2, GL_FLOAT, false, stride, quadBuffer)

        quadBuffer.position(2)
        glEnableVertexAttribArray(texCoordHandle)
        glVertexAttribPointer(texCoordHandle, 2, GL_FLOAT, false, stride, quadBuffer)

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

        glDisableVertexAttribArray(positionHandle)
        glDisableVertexAttribArray(texCoordHandle)
        glDisable(GL_BLEND)
        glDisable(GL_STENCIL_TEST)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glViewport(0, 0, surfaceWidth, surfaceHeight)
    }
}