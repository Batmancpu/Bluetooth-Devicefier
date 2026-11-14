# T700 Companion 0.2.0 — functional foundation

## Evidence and feature status
Input: private sanitized WatchProbe report generated 2026-11-14 23:17 Asia/Kolkata; no raw BLE exchange content in report. Manufacturer string LY716, firmware V09708, hardware string 01, model MI RC, battery 100% at observation, 8 service entries. GATT service/channel map is documented in DEVICE_PROFILE.md.

| Feature | Code delivered | Verified on watch |
|---|---|---|
| Scan and manually select T700 device | Yes | Scanning worked with WatchProbe; new companion not yet tested |
| Connect to watch using BLE | Yes | WatchProbe connection verified; companion not yet tested |
| Standard battery value 2A19 | Yes | 100% via WatchProbe |
| Standard manufacturer, firmware, hardware, model | Yes | LY716 / V09708 / 01 / MI RC via WatchProbe |
| Passive watch notification monitoring via known CCCD 2902 | Optional tap; only 01 00 CCCD writes | NOT tested on device; user must explicitly opt in |
| Local per-session ZIP export (redacted by default) | Yes | Not yet tested on device |
| Proprietary watch setting controls, find watch | Not enabled | Unknown commands |
| Notifications/calls/WhatsApp to watch | Not enabled | Unknown commands |
| Step/HR/sleep/SpO2 data sync | Not enabled | Unknown commands |
| Watch-face image processing/upload | Not enabled | Format and transfer unknown |
| Firmware / DFU / OTA / reset | Intentionally prohibited | Not attempted |

## Safety contract
- Only connect to a manually selected device whose name contains T700.
- Use standard Bluetooth SIG GATT READ on a tightly limited allowlist for battery and Device Information only.
- Optional 'Enable watch notifications' is a distinct, clearly described UI action. It sends ONLY Bluetooth-standard ENABLE_NOTIFICATION_VALUE (hex 01 00) to CCCD descriptor UUID 2902 of previously discovered and specifically permitted NOTIFY channels: 180F/2A19, 6e400001…cca9d/6e400003…cca9d, FFFF/FF11. This is a temporary per-connection subscription rather than a proprietary watch command. Disconnect to stop.
- Never write app data into 6e400002…cca9d or FFFF/FF22, probe random opcodes, request pairing, request DFU, upload watch faces or overwrite firmware. No guessed writes.
- The in-memory notification recorder receives selected-watch-origin event bytes only, keeps at most 256 records (512 bytes each), and exports hex only when explicitly opted in. Default JSON exports timestamps, channel UUID, byte count and aggregate counts; no Bluetooth addresses, no contacts/app notifications, no ambient snoop traffic.
- No Internet permission, analytics, account system, or background service.

## Device test procedure
1. Install the new 'T700 Companion' APK, distinct from WatchProbe.
2. Ensure watch is nearby and not simultaneously held by another companion app. If necessary, close HiWatchPro and WatchProbe, but do NOT factory-reset the watch.
3. Open T700 Companion, grant Nearby Devices, press Scan, tap Connect for the T700 watch.
4. Confirm 'Connected', check battery, firmware, manufacturer and service count. Use Refresh to verify repeated reads.
5. Only if you wish to test safe event observation, tap 'Enable watch notifications (optional)'. This changes a temporary BLE subscription descriptor, not watch settings, and may be rejected by the watch. Do not press any other controls on watch to force it.
6. Export the local ZIP; leave raw payload toggle OFF unless private watch-byte research is needed, and examine the file before sharing.
7. Report install/runtime defects and device behavior. No testing on the actual phone/watch has been performed by the coding agent.

## Limits of present evidence
The WatchProbe report does not include proprietary command packets and the original HiWatchPro 1.3.79 APK bytes are not accessible in this agent's current conversation/container/Library. Historical metadata and a SHA fingerprint do not substitute for real static reverse-engineering. The user may reattach that original exact APK privately to the conversation; do not put proprietary APK into public repo. Do not claim protocol parity from published materials about other watches merely because they share one BLE UUID.

## Build
CI builds two modules and verifies both with Android apksigner, zipalign and aapt:
- :app:assembleDebug → existing WatchProbe, package io.github.batmancpu.watchprobe.
- :companion:assembleDebug → new functional companion, package io.github.batmancpu.watchcompanion.
No emulator/hardware execution is available in the CI.

## Verified build artifact
CI run 43623972393 PASSED on 2026-11-14. Companion artifact ID 14125256837, `companion-debug.apk`, 9,494,460 bytes, SHA256 `8d8f6e160cd03d285009998876f84a4ebf2769a6efae59ad08789bb44f8c47a4`. Android apksigner v2 verifies; zipalign and aapt check pass. SDK min 26, target 35, version 0.2.0. This is a test build with ephemeral Android debug signing; upgrades to later builds may require uninstalling the previous debug variant. Not physically executed yet.
