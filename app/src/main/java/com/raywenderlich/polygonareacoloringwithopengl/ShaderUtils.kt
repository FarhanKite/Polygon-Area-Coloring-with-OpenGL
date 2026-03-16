package com.raywenderlich.polygonareacoloringwithopengl

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object ShaderUtils {

    fun loadShaderFromAssets(context: Context, filename: String): String {
        val inputStream = context.assets.open(filename)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val shaderCode = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            shaderCode.append(line).append("\n")
        }

        reader.close()

        return shaderCode.toString()
    }
}