package com.codephrex.chirp.orb

/** GLES 3 shader sources. Audio inputs are normalized by the future capture pipeline. */
internal object LiquidShaders {
    val vertex = """
        #version 300 es
        precision highp float;
        layout(location=0) in vec3 aPosition;
        uniform mat4 uMvp;
        uniform float uTime;
        uniform float uAmplitude;
        uniform vec3 uBands;
        out vec3 vSurface;
        float radius(vec3 p) {
            float a = clamp(uAmplitude, 0.0, 1.0);
            vec3 bands = clamp(uBands, 0.0, 1.0);
            return 1.0 + 0.012*sin(2.0*uTime)
                + (0.018+0.10*a)*sin(5.0*p.y+3.0*p.x-2.0*uTime)
                + 0.045*bands.x*sin(7.0*p.z+uTime)
                + 0.025*bands.y*sin(11.0*p.x-3.0*uTime)
                + 0.015*bands.z*sin(17.0*p.y+4.0*uTime);
        }
        void main() {
            vSurface = aPosition * radius(aPosition);
            gl_Position = uMvp * vec4(vSurface, 1.0);
        }
    """.trimIndent()
    val fragment = """
        #version 300 es
        precision highp float;
        in vec3 vSurface;
        uniform vec3 uEye;
        uniform vec3 uTint;
        out vec4 fragColor;
        vec3 environment(vec3 d) {
            float strip = pow(max(0.0, dot(d, normalize(vec3(-0.6,0.8,0.4)))), 24.0);
            float rim = pow(max(0.0, dot(d, normalize(vec3(0.7,-0.2,0.5)))), 10.0);
            return vec3(0.008,0.025,0.07) + vec3(0.65,0.85,1.0)*strip
                + vec3(0.02,0.45,0.9)*rim;
        }
        void main() {
            vec3 n = normalize(cross(dFdx(vSurface), dFdy(vSurface)));
            if (dot(n,vSurface)<0.0) n = -n;
            vec3 v = normalize(uEye-vSurface);
            float f = 0.02+0.98*pow(1.0-clamp(dot(n,v),0.0,1.0),5.0);
            vec3 reflected = environment(reflect(-v,n));
            vec3 transmitted = environment(refract(-v,n,0.75));
            vec3 color = mix(transmitted*0.7+uTint*0.22, reflected, f);
            fragColor = vec4(clamp(color,0.0,1.0),1.0);
        }
    """.trimIndent()
}
