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
        // Analytic studio environment, not a captured HDR environment map.
        vec3 environment(vec3 d) {
            d = normalize(d);
            float key = pow(max(0.0, dot(d, normalize(vec3(-0.45,0.65,0.65)))), 12.0);
            float strip = exp(-36.0*pow(d.x+0.38,2.0))*smoothstep(-0.15,0.65,d.y);
            float fill = pow(max(0.0, dot(d, normalize(vec3(0.8,-0.35,0.45)))), 8.0);
            return vec3(0.015,0.045,0.11) + vec3(2.4,3.2,4.0)*key
                + vec3(0.65,1.5,2.2)*strip + vec3(0.05,0.65,1.8)*fill;
        }
        void main() {
            vec3 n = normalize(cross(dFdx(vSurface), dFdy(vSurface)));
            if (dot(n,vSurface)<0.0) n = -n;
            vec3 v = normalize(uEye-vSurface);
            float nv = clamp(dot(n,v),0.0,1.0);
            float f = 0.0204+0.9796*pow(1.0-nv,5.0);
            vec3 ray = refract(-v,n,1.0/1.333);
            // Approximate exit surface with a sphere; deformed mesh is not ray traced.
            float travel = max(0.0,-2.0*dot(vSurface,ray));
            vec3 exitNormal = normalize(vSurface+travel*ray);
            vec3 exitRay = refract(ray,-exitNormal,1.333);
            if (dot(exitRay,exitRay)<0.001) exitRay = reflect(ray,exitNormal);
            vec3 absorption = exp(-vec3(1.15,0.32,0.09)*travel);
            vec3 transmitted = environment(exitRay)*absorption;
            vec3 reflected = environment(reflect(-v,n));
            vec3 light = normalize(vec3(-0.45,0.65,0.65));
            float spec = pow(max(0.0,dot(n,normalize(light+v))),72.0);
            float rim = pow(1.0-nv,2.5);
            vec3 color = mix(transmitted,reflected,f)
                + uTint*(0.10+0.28*rim)
                + vec3(1.8,2.5,3.0)*spec;
            // Linear-light exposure and display encoding for the RGBA8 preview.
            color = vec3(1.0)-exp(-max(color,vec3(0.0))*1.25);
            fragColor = vec4(pow(color,vec3(1.0/2.2)),1.0);
        }
    """.trimIndent()
}
