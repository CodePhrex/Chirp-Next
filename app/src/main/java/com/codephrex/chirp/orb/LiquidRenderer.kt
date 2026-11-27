package com.codephrex.chirp.orb

import android.opengl.GLES30 as GL
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** GL-thread owned renderer; a new context recreates all GPU objects. */
internal class LiquidRenderer(private val onFailure: (String) -> Unit) : GLSurfaceView.Renderer {
    data class Audio(val amplitude: Float = 0f, val low: Float = 0f,
                     val mid: Float = 0f, val high: Float = 0f)
    @Volatile var audio = Audio()
    private var program = 0
    private val buffers = IntArray(2)
    private val vao = IntArray(1)
    private var count = 0
    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val mvp = FloatArray(16)
    private var start = 0L
    private val uniforms = HashMap<String, Int>()

    private fun shader(type: Int, source: String): Int {
        val id = GL.glCreateShader(type)
        check(id != 0) { "Shader allocation failed" }
        GL.glShaderSource(id, source)
        GL.glCompileShader(id)
        val status = IntArray(1)
        GL.glGetShaderiv(id, GL.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GL.glGetShaderInfoLog(id)
            GL.glDeleteShader(id)
            error("Shader compilation: $log")
        }
        return id
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        program = 0
        buffers.fill(0)
        vao.fill(0)
        uniforms.clear()
        var vertex = 0
        var fragment = 0
        try {
            vertex = shader(GL.GL_VERTEX_SHADER, LiquidShaders.vertex)
            fragment = shader(GL.GL_FRAGMENT_SHADER, LiquidShaders.fragment)
            program = GL.glCreateProgram()
            check(program != 0)
            GL.glAttachShader(program, vertex)
            GL.glAttachShader(program, fragment)
            GL.glLinkProgram(program)
            val status = IntArray(1)
            GL.glGetProgramiv(program, GL.GL_LINK_STATUS, status, 0)
            check(status[0] != 0) { GL.glGetProgramInfoLog(program) }
            for (name in listOf("uMvp", "uTime", "uAmplitude", "uBands", "uEye", "uTint")) {
                uniforms[name] = GL.glGetUniformLocation(program, name).also { check(it >= 0) }
            }
            val mesh = LiquidMesh.sphere()
            count = mesh.indices.size
            val vertices = ByteBuffer.allocateDirect(mesh.positions.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
            vertices.put(mesh.positions).position(0)
            val indices = ByteBuffer.allocateDirect(count * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer()
            indices.put(mesh.indices).position(0)
            GL.glGenVertexArrays(1, vao, 0)
            GL.glGenBuffers(2, buffers, 0)
            GL.glBindVertexArray(vao[0])
            GL.glBindBuffer(GL.GL_ARRAY_BUFFER, buffers[0])
            GL.glBufferData(GL.GL_ARRAY_BUFFER, mesh.positions.size * 4, vertices, GL.GL_STATIC_DRAW)
            GL.glEnableVertexAttribArray(0)
            GL.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 12, 0)
            GL.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, buffers[1])
            GL.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, count * 2, indices, GL.GL_STATIC_DRAW)
            GL.glBindVertexArray(0)
            GL.glEnable(GL.GL_DEPTH_TEST)
            GL.glClearColor(0.008f, 0.027f, 0.067f, 1f)
            check(GL.glGetError() == GL.GL_NO_ERROR) { "GPU initialization failed" }
            start = System.nanoTime()
        } catch (e: Exception) {
            release()
            onFailure(e.message ?: "Renderer initialization failed")
        } finally {
            if (vertex != 0) GL.glDeleteShader(vertex)
            if (fragment != 0) GL.glDeleteShader(fragment)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GL.glViewport(0, 0, width, height)
        val aspect = width.coerceAtLeast(1).toFloat() / height.coerceAtLeast(1)
        val scale = maxOf(1f, 1f / aspect)
        Matrix.perspectiveM(projection, 0, 40f, aspect, 0.1f, 30f)
        Matrix.setLookAtM(view, 0, 0f, 0f, 4.5f * scale, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mvp, 0, projection, 0, view, 0)
    }

    private fun normalized(value: Float) = if (value.isFinite()) value.coerceIn(0f, 1f) else 0f
    override fun onDrawFrame(gl: GL10?) {
        GL.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
        if (program == 0) return
        val frame = audio
        GL.glUseProgram(program)
        GL.glUniformMatrix4fv(uniforms.getValue("uMvp"), 1, false, mvp, 0)
        GL.glUniform1f(uniforms.getValue("uTime"), ((System.nanoTime() - start) / 1e9).toFloat())
        GL.glUniform1f(uniforms.getValue("uAmplitude"), normalized(frame.amplitude))
        GL.glUniform3f(uniforms.getValue("uBands"), normalized(frame.low), normalized(frame.mid), normalized(frame.high))
        GL.glUniform3f(uniforms.getValue("uEye"), 0f, 0f, 4.5f)
        GL.glUniform3f(uniforms.getValue("uTint"), 0f, 0.42f, 1f)
        GL.glBindVertexArray(vao[0])
        GL.glDrawElements(GL.GL_TRIANGLES, count, GL.GL_UNSIGNED_SHORT, 0)
        GL.glBindVertexArray(0)
    }

    /** Call only on the GL thread with the owning context current. */
    fun release() {
        if (program != 0) GL.glDeleteProgram(program)
        GL.glDeleteBuffers(2, buffers, 0)
        GL.glDeleteVertexArrays(1, vao, 0)
        program = 0
        buffers.fill(0)
        vao.fill(0)
    }
}
