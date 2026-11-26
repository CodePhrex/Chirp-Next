# Chirp! architecture and acceptance
## Phase 1–2 boundary
Build a standalone native Android orb laboratory before social screens. Proposed baseline: minSdk 26, Java 17, Kotlin/Compose, GLES 3.0 with capability detection. Pin compatible build versions during initialization. Unsupported graphics must show an explicit capability message, not a fake accepted orb.
## Boundaries
- app: Compose navigation, permissions, lifecycle, accessible status and controls.
- orb-core: pure Kotlin immutable state, event reducer, effects and normalized audio envelope; no Android dependency.
- orb-renderer: GL thread owns mesh, shader programs and textures; immutable snapshots cross threads; release resources on context loss and recreate on resume.
- audio: permission-gated capture, bounded buffers, RMS and frequency features; no recording or network transmission in offline lab. Future WebRTC adapter owns transport.
- haptics: transition effects mapped to supported Android vibration APIs, user opt-out, no frame-triggered vibration.
## State and lifecycle
Nine approved states; transport confirmation and microphone permission required before actual transmitting. Local lab must explicitly label preview, not imply live communication. Pointer release/cancel, backgrounding, permission revocation and transport failure stop capture and release floor. Use session IDs to ignore stale callbacks. Clamp audio features, smooth attack/release, avoid allocation on render loop. Reduced motion preserves status text and interaction.
## Future service boundaries
Authentication via Firebase; authenticated WebSocket presence and floor signaling, WebRTC/Opus media, PostgreSQL durable accounts/channel membership, Redis ephemeral presence/floor leases, FCM notifications, Room local preferences. Server authorizes channel membership and floor leases; never trust client status. No backend provisioning in phase 1.
## Acceptance matrix
| Gate | Evidence |
|---|---|
| CH-003 artwork | Original PNG bytes in assets/reference, verified SHA-256 against supplied originals |
| CH-004 design | Reviewed tokens and reference-led design rules |
| CH-006 architecture | Reviewed module boundaries and lifecycle contract |
| CH-007 roadmap | Seven phases in BLUEPRINT plus this evidence matrix |
| CH-008 Android | Clean checkout builds debug APK with pinned toolchain |
| CH-009 CI | PR unit tests, lint and assembleDebug succeed; APK artifact available |
| CH-010 prototype | Real shader/mesh rendering, nine-state reducer, touch cancellation tests; prototype explicitly labeled |
| Phase 2 exit | Physical device screenshots and comparison to both originals, frame timing, real microphone response, haptic and accessibility checks, user visual approval |
## Test strategy
Pure reducer tests for permission denial, stale events, release/cancel, disconnect and all nine states. Instrumented lifecycle and touch tests; shader compile/link diagnostics on device; frame timing at idle and active; microphone privacy checks; manual haptic verification. Target 60 FPS on supported hardware, report measured results and device model. No expansion into phase 3 until phase 2 gate passes.
## Progress and blockers
CH-001/002/005 on main. CH-004 PR #1 open at inspection; no CI runs or statuses reported, no reviews or review threads. CH-003 original binary upload remains unverified; request original PNG upload if unavailable. CH-006/007 proposed here. No APK, tests or device validation yet. Next: Android initialization and CI after review of foundation changes.
