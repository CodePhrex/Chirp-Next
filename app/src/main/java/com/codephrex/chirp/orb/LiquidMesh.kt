package com.codephrex.chirp.orb

import kotlin.math.*

/** Unit sphere with shared poles and wrapped longitude, for GLES vertex deformation. */
class LiquidMesh private constructor(val positions: FloatArray, val indices: ShortArray) {
    companion object {
        fun sphere(rings: Int = 48, segments: Int = 64): LiquidMesh {
            require(rings in 2..128 && segments in 3..128)
            val count = 2 + (rings - 1) * segments
            val p = FloatArray(count * 3)
            p[1] = 1f
            p[(count - 1) * 3 + 1] = -1f
            fun index(r: Int, s: Int) = 1 + (r - 1) * segments + s % segments
            for (r in 1 until rings) {
                val theta = PI * r / rings
                for (s in 0 until segments) {
                    val phi = 2 * PI * s / segments
                    val i = index(r, s) * 3
                    p[i] = (sin(theta) * cos(phi)).toFloat()
                    p[i + 1] = cos(theta).toFloat()
                    p[i + 2] = (sin(theta) * sin(phi)).toFloat()
                }
            }
            val out = ShortArray(6 * segments * (rings - 1))
            var k = 0
            fun tri(a: Int, b: Int, c: Int) {
                out[k++] = a.toShort(); out[k++] = b.toShort(); out[k++] = c.toShort()
            }
            for (s in 0 until segments) {
                tri(0, index(1, s + 1), index(1, s))
                for (r in 1 until rings - 1) {
                    val a = index(r, s); val b = index(r, s + 1)
                    val c = index(r + 1, s); val d = index(r + 1, s + 1)
                    tri(a, b, c); tri(b, d, c)
                }
                tri(index(rings - 1, s), index(rings - 1, s + 1), count - 1)
            }
            check(k == out.size)
            return LiquidMesh(p, out)
        }
    }
}
