# T700 Companion — functional implementation and verification plan
Updated 2026-11-15. Active module: companion/ only. WatchProbe retired from main.

## 0. Physical evidence baseline (verified)
Latest user-supplied T700-Companion-20261115-0109.zip was inspected byte-for-byte:
- version 0.2.0, physical watch connected, 8 GATT service entries
- battery 100%; manufacturer LY716, firmware V09708, hardware 01, model MI RC
- standard GATT 0x2902 subscriptions succeeded for 2A19, vendor 6e400003-...-cca9d, FFFF/FF11
- exactly 2 vendor-channel notifications of 20 bytes each, 6e400003-...-cca9d
- raw watch bytes were deliberately omitted, so payload semantics, clocks, and opcodes are UNKNOWN
- no vendor writes/OTA/DFU performed.
- session log records multiple scan attempts while phone Bluetooth was OFF and repeated scan presses; v0.3.0 addresses this in the UI and with scanning guard.

## 1. Baseline build now — v0.3.0 (implemented; physical v0.3 tests pending)
A. Manual discovery/connection: T700-name only, avoid collecting unrelated nearby device names, improve repeated-scan handling, prompt Android user to enable Bluetooth instead of merely logging 'off'. No auto bonding. Read 2A19 battery and observed read-only Device Information.
B. Observed GATT: explicit tap to subscribe to battery 2A19, vendor 6e400003...cca9d and FFFF/FF11 using standard temporary CCCD (0x2902) writes only; no application-level command writes. Per-session count and bounded 256 watch-only packets in RAM. Default ZIP redacted, optional watch-only raw bytes (sensitive).
C. Guided test lab: start/stop intervals for normal watch screen/button, watch fitness screen, WhatsApp message, SMS message, phone call and local face preview; count correlated watch packets and phone notification metadata in that interval. Counts indicate only correlation, NOT protocol decoding or that a feature works.
D. Android phone event detector: separate system-authorized NotificationListenerService for selected WhatsApp, SMS, Telegram and phone/dialer packages. Additional explicit in-app switch. Captures only source category, kind and time (max 100), no text, sender, contact, number, notification ID, or payload. No forwarding to watch. Do not request READ_SMS, READ_CONTACTS, READ_CALL_LOG, READ_PHONE_STATE or internet.
E. Watch-face local conversion: user selects local image, app reads size, center-crops/scales to user-selected 64–640 pixel dimensions and saves standalone PNG offline. Does NOT imply correct watch resolution or compatible upload format; no BLE transfer.
F. Time: displays phone clock only. Watch-specific time sync is NOT enabled.
G. ZIP export with device read state, watch event channels/size/times, optional raw watch-origin bytes, phone metadata (no content), guided test windows/results and face preview metadata. No Bluetooth address.
H. Test pure safety/utility helpers with JUnit, compile Android 35 target, Android apksigner verify, zipalign, aapt package inspection in CI.

## 2. Protocol reconstruction — required before watch-changing functionality
Primary evidence: exact HiWatchPro 1.3.79 APK, size 111842623 bytes, SHA-256 d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc. The file was supplied in an older conversation, but its bytes are NOT available in this accessible container, conversation upload list, or Files Library at writing. Do not claim current static inspection or obtain a potentially modified APK from random mirrors.
Extract APK's complete manifest and resources, DEX call graph, BLE GATT UUID mapping, encode/decode code paths, checksums/framing, time sync data shape, notification payloads, call handling, health/steps protocol, watch-face image sizes, package generation, acknowledgements and transfer retries. Cross-check with physical report. Presence of SDKs for other watches does not prove this watch uses them.
One optional alternative: bounded watch-only raw notifications from v0.3.0 when user explicitly opts in, plus user-labeled test windows. These can reveal inbound messages but cannot alone prove outbound request bytes. If necessary, ask for a user-provided HCI log, warning that it may contain activity from unrelated devices and should not be shared unfiltered. Do not bypass Android's isolation of other apps.

## 3. Feature implementation order, only after exact protocol proof
P1: time synchronization. Need byte-exact packet from original companion tied to this device; date format/timezone/ack handling; action explicitly tapped; validate clock before and after manually. Never infer from phone clock alone.
P2: notification text and message forwarding. Require byte-level app-to-watch packet, recipient type/encoding/chunking/ack; Android notification listener permission; whitelist opt-in apps and privacy controls, duplicate suppression and queue backpressure; test local-only metadata before first watch transmission.
P3: incoming call alerts. Require exact command and identify watch's call UI/interaction. Avoid asking call log/contacts permissions unless feature truly needs them; test with one user-approved mock or real call; no unintended auto-answer.
P4: alarm/settings/find-watch. For every operation document exact packet, source, reversibility and failure modes. Require separate user tap for each test. Do not send any unknown command.
P5: watch health/steps/sleep data. Prefer read-only transaction where possible, confirm decoder with checksums/length limits and personal health privacy. Avoid indiscriminate raw archive; no cloud.
P6: watch-face upload. First determine actual pixel format and display size from exact app implementation, build offline validated conversion, checksum/package generator, local simulator/fixtures, then small user-selected transfer with verified ack and no firmware/OTA. No writes to firmware/DFU, unknown flash segments or bootloader.
P7: always-on operation. Introduce only after P1-P6 and device tests, with Android 15 background restrictions, service lifecycle, permission-minimization, reconnect backoff and measured battery consumption. Do not promise <1%/day without measurement.
P8: only after functionality, premium UI/aesthetics.

## 4. Strict device safety gate before enabling any vendor write
Every command must have: watch-specific source evidence; exact bytes/parameters/checksum/expected response; no DFU or factory-reset association; bounded queue with timeout, explicit UI action, cancellation, clear description of on-watch changes and recovery strategy. If any are unknown, feature remains disabled. CCCD 01 00 per-connection enabling is a standard exception already proven by physical watch evidence and is separately tapped.

## 5. v0.3.0 physical test checklist
1. Install companion v0.3.0 (debug signing may require uninstalling 0.2.0 first). Grant Nearby Devices. No Internet permission.
2. With phone Bluetooth OFF, tap Scan; confirm Android permission/enable UI. Enable it; scan once and confirm no repetitive scan flood. Keep previous proprietary companion closed.
3. Connect T700; verify "Connected", 8 services, battery and LY716/V09708/01/MI RC. Refresh reads twice.
4. Tap temporary CCCD observation; verify 3 subscriptions and any vendor incoming message counts. Disconnect afterward.
5. For phone events, grant system Notification Access to T700 Companion, return, enable in-app metadata switch. Receive a test WhatsApp and a test SMS/phone call; verify only counters/category/time are shown. This is NOT forwarding to watch.
6. Run "Incoming WhatsApp" or "Watch screen/button action" test while BLE observation runs. Finish. Confirm guided-test packet/phone counts. A 0 count does not imply unavailable feature.
7. Pick a local image and export offline PNG; inspect it separately. Watch face on watch must remain untouched.
8. Export research ZIP with raw toggle OFF initially; use raw ON only when specifically deciding to share bounded watch-origin bytes privately, because even watch-only payloads may encode personal information.
9. Report screen/error/feature behavior. Never test firmware/OTA/reset or undocumented writes.


### v0.3.0 release verification — 2026-11-15
GitHub Actions run 43634806135 (source commit 5086f0d5fff1b2942098c51514051cc6e829c8dc) completed SUCCESS: :companion:testDebugUnitTest, :companion:assembleDebug, Android apksigner v2, zipalign, aapt metadata. Package io.github.batmancpu.watchcompanion, version 0.3.0/versionCode 3, minSdk 26, targetSdk 35, label T700 Companion. Artifact ID 14130235385, name T700-Companion-0.3.0-debug-APK. Extracted companion-debug.apk is 9,511,020 bytes; SHA-256 6c00c77fa0cf89e910e991d1c972a8a405e7eef4f537e53b74b60b4c616437a5. Both artifact ZIP and APK archive integrity passed. This is still a CI debug-signed APK, may require uninstall of previous CI-signed 0.2.0 due signing certificate mismatch. Actual v0.3.0 execution on watch/phone remains pending user verification. No developer-side watch operations performed.


## 2026-11-15 — v0.3.1 Play Protect compatibility and strict watch-only connection
User reports the sideloaded v0.3.0 APK is **blocked by Google Play Protect**, and wants their usually connected earbuds left entirely alone. Google Play Protect developer guidance and Google's enhanced fraud-protection announcement for India identify BIND_NOTIFICATION_LISTENER_SERVICE as a sensitive capability that can trigger installation blocking of apps sideloaded from messaging/file/browser sources. This is a **likely explanation, not a proven root cause without the exact dialog**. Never recommend disabling Play Protect or disguising permissions.
**Changes:** For v0.3.1, remove the Android NotificationListenerService entirely, including the class, manifest service declaration, system permission settings button, automatic phone event storage, and corresponding test/assertion. Retain manual WhatsApp/SMS/call *named test windows* only: the app can count incoming watch-origin BLE packets while user manually performs an action, but it cannot detect or forward phone messages/calls. No Internet, Accessibility, SMS, contacts or call-log permissions. Add build-time checks to reject a manifest that declares the sensitive notification listener or INTERNET.
**Watch/earbud isolation:** BLE scanner now uses exact advertised-name scan filters (`T700Pro Max` and `T700 Pro Max`) and a second exact-name check before collecting/connecting. Connection action rejects devices not present in that scan; after GATT discovery, reject any peripheral lacking the watch's previously verified vendor service `6e400001-b5a3-f393-e0a9-e50e24dcca9d`, `FFFF` and `180F` battery service. No pairing, device-list enumeration, system-audio controls, audio-profile API use, Bluetooth adapter resets or changes to existing earbud links. Only a GATT belonging to the selected watch is closed. No hardware test by agent; actual device's name/services were previously observed.
**Version:** `T700 Companion v0.3.1`, versionCode 4, package unchanged `io.github.batmancpu.watchcompanion`, still CI-generated debug signed. Certificate can differ across CI builds; may require uninstall of earlier 0.2.0/0.3.0 debug APK before installing. Removal of the listener **does not guarantee** Play Protect installation acceptance, as APK reputation and other checks exist. Capture the exact wording or screenshot of any remaining Play Protect block and use Google's official developer appeal/review process if incorrect. Tests/build pending CI at this entry.

