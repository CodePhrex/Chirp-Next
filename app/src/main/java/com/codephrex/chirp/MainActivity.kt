package com.codephrex.chirp

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.codephrex.chirp.orb.LiquidRenderer

class MainActivity : ComponentActivity() {
    private var surface: GLSurfaceView? = null
    private var resumed = false
    private var failure by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val supported = manager.deviceConfigurationInfo.reqGlEsVersion >= 0x30000
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Chirp! Orb Laboratory", style = MaterialTheme.typography.headlineSmall)
                    Text("GPU preview • audio and transmission not connected")
                    if (!supported) {
                        Text("OpenGL ES 3.0 is required for this preview.")
                    } else {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            factory = { context ->
                                GLSurfaceView(context).apply {
                                    setEGLContextClientVersion(3)
                                    setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                                    setRenderer(LiquidRenderer { reason ->
                                        runOnUiThread { if (!isDestroyed) failure = reason }
                                    })
                                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                                    surface = this
                                    if (!resumed) onPause()
                                }
                            }
                        )
                        failure?.let { Text("Renderer error: " + it, color = MaterialTheme.colorScheme.error) }
                    }
                    Text("Visual fidelity and physical-device performance are not yet validated.")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        surface?.onResume()
    }

    override fun onPause() {
        resumed = false
        surface?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        surface = null
        super.onDestroy()
    }
}
