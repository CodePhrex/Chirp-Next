# CH-013 — Liquid shader foundation

GLES 3 vertex source deforms the indexed sphere with bounded amplitude and three frequency-band uniforms. Fragment source derives surface normals from derivatives and uses analytic environment lighting, Fresnel reflection and approximate refraction. This is not a complete renderer or verified artwork match.

Next: compile/link on GLES context, bind mesh buffers and uniforms, integrate lifecycle and real microphone input, test on physical Android hardware. CI and shader compilation are not yet verified. Original chirp-orb1.png and chirp-orb2.png remain outstanding from prior inspection; visual acceptance blocked. Legacy CHIRP unchanged.
