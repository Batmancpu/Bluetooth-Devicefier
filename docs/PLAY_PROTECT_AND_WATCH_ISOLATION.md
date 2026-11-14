# Play Protect sideload review & Bluetooth isolation

Date: 2026-11-15 | Current active app: T700 Companion v0.3.1

## Play Protect report
The user reports v0.3.0 installation was blocked on their OPPO Android 15. Original Play Protect dialog text is not available. Version 0.3.0 declared a `NotificationListenerService` bound via `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`; that component can access sensitive notifications even though the implementation attempted to record only metadata after opt-in.

Google's enhanced fraud protection in India can block browser/file-manager/messaging-app sideloads that declare sensitive SMS/notification listener/accessibility components:
- https://blog.google/intl/en-in/products/launching-enhanced-fraud-protection-pilot-in-india/
- https://developers.google.com/android/play-protect/warning-dev-guidance
- https://support.google.com/android/answer/2812853

Version 0.3.1 genuinely removes the listener *from both source and manifest*, all phone-side monitoring UI and exports. Android requests only Bluetooth Nearby Devices (SCAN, CONNECT) and legacy platform Bluetooth/location permissions on older Android versions. No INTERNET, SMS, call logs, contact or Accessibility permissions. GitHub Actions statically checks the generated manifest to ensure Notification Listener binding is absent and checks no Internet permission is introduced. This is permission minimization, not a trick to disable security checking. It may still be blocked for unrelated reasons. If blocked again, request the **precise dialog text/screenshot**, retain Play Protect enabled, audit any Google security finding and if a false positive follow the official appeal procedure.

Reintroduce NotificationListenerService only after sufficient product readiness for legitimate wearable notification relay and suitable distribution/review; require explicit user approval and privacy controls. Manual user-labelled phone-call/WhatsApp/SMS test windows can remain with watch-origin BLE packets only; no phone message contents are accessed and **no notification forwarding to the watch is implemented**.

## Earbuds / other Bluetooth devices
- BLE scan uses `ScanFilter.setDeviceName` for only `T700Pro Max` and `T700 Pro Max`; onScanResult independently rejects all other advertised names.
- Only manually tapped items in the filtered T700 results can be connected; no bond operations.
- After service discovery, the selected peripheral must expose `180F`, `FFFF` and the exact `6e400001-b5a3-f393-e0a9-e50e24dcca9d` service seen on the physical watch.
- App only calls `connectGatt` / `disconnect` / `close` on **its own selected BLE connection**. No Bluetooth A2DP/HFP/audio profile operations, audio device pairing, disconnects, or adapter restarts. Connected earbuds can remain in use.
- Stronger identity verification such as locally pinned MAC can be added after real-device validation, accounting for Bluetooth privacy random addresses. Do not store identifiers in public reports.

## Evidence and tests
Physical v0.2.0 report proved the watch has all three services (8 service entries), battery 100%, and transmitted two 20-byte watch-origin notifications after temporary CCCD enable. This is the evidence for profile gating. The new strict filtering/Play Protect compatibility changes require v0.3.1 actual install and watch test; CI cannot confirm device policy acceptance or physical coexistence with earbuds.

## v0.3.1 packaging verification, 2026-11-15
CI run https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43636700958 for source commit 4377eb822cf1fb307b4b1fcdc7a2bcbdabe407c3 completed SUCCESS. Gradle :companion:testDebugUnitTest and :companion:assembleDebug PASSED; official Android apksigner v2 verification PASSED; zipalign and aapt badging PASSED. CI asserts compiled manifest does not contain BIND_NOTIFICATION_LISTENER_SERVICE and does not request INTERNET. Package io.github.batmancpu.watchcompanion, versionCode 4/versionName 0.3.1, minSdk 26, targetSdk 35. Artifact ID 14130278568, name T700-Companion-0.3.1-debug-APK. Downloaded artifact, extracted companion-debug.apk, unzip -t PASS; file size 9,510,844 bytes, SHA-256 e0e310841c5891076d868f1a3e7c121bbd00dd7d4d01bd349f0f6114d71b8ccb. Delivered exact file as T700-Companion-v0.3.1-watch-only.apk. This does NOT establish Google Play Protect acceptance or on-device operation; user must test. Debug signing identity may be ephemeral and incompatible with previous installs; long-term stable release signing is pending. No physical-watch operations performed by agent.
