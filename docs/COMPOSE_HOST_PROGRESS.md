# CH-015 — Compose GLES host

2026-11-27: Connect LiquidRenderer to the Orb Laboratory via AndroidView and GLSurfaceView. Request GLES3 and a depth buffer, pause/resume with Activity, report renderer errors on UI thread, and show unsupported hardware explicitly. No microphone or transmission is connected. CI and physical-device validation pending.

Smoke test: install debug APK on GLES3 Android; verify actual orb appears, background/foreground and rotate repeatedly, inspect logcat for EGL/shader failures, record frame performance and screenshots. Verify unsupported hardware message where possible. Original chirp-orb1.png and chirp-orb2.png still required for fidelity acceptance. Next: live audio capture and state integration after host validation. No legacy changes.
