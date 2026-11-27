# CH-016 — PCM analysis foundation

2026-11-27: Added pure Kotlin PCM16 RMS, DC removal, Hann-windowed FFT, three frequency bands and attack/release smoothing. Regression tests cover silence, DC, band separation, decay and reset. CI pending; no local or physical tests claimed. This is not microphone capture or transmission. Next: permission-gated AudioRecord lifecycle, fixed-size frame accumulation, renderer binding and cancellation. Original reference PNGs remain absent from main tree; visual acceptance blocked. Legacy CHIRP untouched.
