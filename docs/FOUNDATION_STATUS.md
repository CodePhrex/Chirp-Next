# Foundation status
CH-008/009: Android bootstrap and CI proposed; build evidence pending. The lab deliberately displays a text-only implementation status, not a substitute orb. Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21, SDK 35 and Java 17 are proposed build pins. CI uses installed Gradle pending a verified wrapper. CH-003: original PNGs not verified in repository; upload exact originals and record SHA-256 before visual acceptance. PRs 1 and 2 remain open with no reported workflow runs or status checks at inspection. Next: verify CI, preserve artwork, implement state machine and renderer. Physical-device acceptance remains outstanding.

## CI follow-up
Run 45741100377 failed before compilation: setup-android attempted obsolete SDK package `tools`. Commit c7af2737fcb891891cd51d208e4d39fcfa1ac829 replaces that action with hosted-runner sdkmanager provisioning of API 35 and build tools 35.0.0. Recheck CI before merge; no APK or physical-device validation yet. Original artwork upload remains pending.

## Second CI follow-up
Run 45753424666 failed on Ubuntu 26.04 at `command -v sdkmanager` before compilation. Commit 1ab5c6040208b3a55f714c5bff603af87ca99380 pins Ubuntu 24.04, resolves sdkmanager from SDK root with PATH fallback, and upgrades setup-java to v5 while retaining Java 17. Compatibility and all build gates remain pending. No merge or device verification. CH-003 artwork still pending.
