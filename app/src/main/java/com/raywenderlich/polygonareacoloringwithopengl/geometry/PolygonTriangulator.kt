package com.raywenderlich.polygonareacoloringwithopengl.geometry

object PolygonTriangulator {

    fun triangulate(verts: FloatArray): FloatArray {
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

    fun isPointInsideTriangulation(glX: Float, glY: Float, triangulatedVertices: FloatArray): Boolean {
        var i = 0
        while (i < triangulatedVertices.size - 5) {
            if (pointInTriangle(
                    glX, glY,
                    triangulatedVertices[i],     triangulatedVertices[i + 1],
                    triangulatedVertices[i + 2], triangulatedVertices[i + 3],
                    triangulatedVertices[i + 4], triangulatedVertices[i + 5]
                )
            ) return true
            i += 6
        }
        return false
    }

    private fun isCounterClockwise(verts: FloatArray, indices: ArrayDeque<Int>): Boolean {
        var area = 0f
        val n = indices.size
        for (i in 0 until n) {
            val c = indices[i]
            val nx = indices[(i + 1) % n]
            area += verts[c * 2] * verts[nx * 2 + 1] - verts[nx * 2] * verts[c * 2 + 1]
        }
        return area > 0f
    }

    private fun isEar(verts: FloatArray, indices: ArrayDeque<Int>, p: Int, c: Int, n: Int): Boolean {
        val ax = verts[p * 2]; val ay = verts[p * 2 + 1]
        val bx = verts[c * 2]; val by = verts[c * 2 + 1]
        val cx = verts[n * 2]; val cy = verts[n * 2 + 1]
        if (cross(ax, ay, bx, by, cx, cy) <= 0f) return false
        for (idx in indices) {
            if (idx == p || idx == c || idx == n) continue
            if (pointInTriangle(verts[idx * 2], verts[idx * 2 + 1], ax, ay, bx, by, cx, cy)) return false
        }
        return true
    }

    private fun cross(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float) =
        (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)

    private fun pointInTriangle(
        px: Float, py: Float,
        ax: Float, ay: Float,
        bx: Float, by: Float,
        cx: Float, cy: Float
    ): Boolean {
        val d1 = cross(ax, ay, bx, by, px, py)
        val d2 = cross(bx, by, cx, cy, px, py)
        val d3 = cross(cx, cy, ax, ay, px, py)
        return !((d1 < 0f || d2 < 0f || d3 < 0f) && (d1 > 0f || d2 > 0f || d3 > 0f))
    }

    private fun MutableList<Float>.addTriangle(verts: FloatArray, i0: Int, i1: Int, i2: Int) {
        add(verts[i0 * 2]); add(verts[i0 * 2 + 1])
        add(verts[i1 * 2]); add(verts[i1 * 2 + 1])
        add(verts[i2 * 2]); add(verts[i2 * 2 + 1])
    }
}