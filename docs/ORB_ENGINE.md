# Signature Orb Engine
The orb is the central interaction object, not decoration. Original reference images govern visual acceptance.
## State contract
| State | Visual | Haptic |
|---|---|---|
| Idle | Calm blue sphere, gentle breathing | None |
| Transmitting | Cyan liquid deformation driven by local audio | Firm activation pulse |
| Receiving | Incoming audio ripples and droplets | Double soft pulse |
| Connected | Continuous energy orbit | Confirmation |
| Transition | Smooth restoration to natural shape | Light release |
| Busy | Amber illumination | Warning |
| DND | Restrained purple glow | None |
| Offline | Dim, nearly motionless | None |
| Error | Brief red pulse | Triple pulse |
## Implementation
Kotlin state machine, Compose host, OpenGL ES shader renderer, procedural mesh, audio amplitude/frequency analysis, Android VibrationEffect. Press-and-hold to request transmission; release or cancellation stops capture. Actual transmission state must reflect connection and permission status. Haptics configurable and triggered by transitions, not every frame.
## Acceptance
Dynamic rendering, liquid refraction and reflections faithful to original art; responsive touch, real audio-driven deformation, distinguishable accessible states, graceful permission and connection failure, adaptive rendering and measured performance on physical devices. No prerecorded GIF as production renderer. Target 60 FPS on supported hardware.
