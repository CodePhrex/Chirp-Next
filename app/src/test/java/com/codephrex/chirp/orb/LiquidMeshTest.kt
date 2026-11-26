package com.codephrex.chirp.orb
import org.junit.Assert.*
import org.junit.Test

class LiquidMeshTest {
    @Test fun unitSphereAndValidIndices() {
        val mesh = LiquidMesh.sphere(12, 16)
        assertEquals(178 * 3, mesh.positions.size)
        assertEquals(6 * 16 * 11, mesh.indices.size)
        for (i in mesh.positions.indices step 3) {
            val x = mesh.positions[i]; val y = mesh.positions[i+1]; val z = mesh.positions[i+2]
            assertEquals(1f, x*x+y*y+z*z, 0.00001f)
        }
        assertTrue(mesh.indices.all { (it.toInt() and 65535) < mesh.positions.size / 3 })
        for (i in mesh.indices.indices step 3) {
            assertNotEquals(mesh.indices[i], mesh.indices[i+1])
            assertNotEquals(mesh.indices[i], mesh.indices[i+2])
            assertNotEquals(mesh.indices[i+1], mesh.indices[i+2])
        }
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidResolution() { LiquidMesh.sphere(1, 16) }
}
