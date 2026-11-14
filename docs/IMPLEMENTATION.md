# Historical document: WatchProbe retired on 2026-11-15

The WatchProbe source and Gradle module were removed from active main at user's direction; use this file only as historical architectural and safety reference. Current Android code is in `companion/`, and current build instructions are in `docs/COMPANION_STAGE1.md`.

# WatchProbe 0.1.0 — technical implementation and test plan

## Delivered source
- Native Android Kotlin/Jetpack Compose, minSdk 26, targetSdk 35.
- Android 12+ nearby devices permissions: BLUETOOTH_SCAN (neverForLocation) and BLUETOOTH_CONNECT; older Android uses ACCESS_FINE_LOCATION for BLE scanning.
- No INTERNET permission, telemetry, ads, accounts, location collection or background process.
- Scanner: manually started, 15-second advertising observation, manufacturer IDs, service UUIDs, RSSI, legacy/connectable flags; raw advertising bytes held only in process memory until optional explicit export.
- Watch selection is manual. Connect with BluetoothGatt in LE transport and discover all exposed services and characteristics, enumerating descriptor UUIDs/properties/permissions.
- Strict reads: ONLY characteristic PROPERTY_READ with no READ_ENCRYPTED or READ_ENCRYPTED_MITM permissions. Sequential GATT reads with a 5-second per-read timeout. No descriptor reads/writes, no notification subscription, no characteristic writes, no createBond, no OTA or firmware access.
- Optional local HCI snoop log import: only uncompressed btsnoop version 1, datalink 1002 (H4), up to 32 MiB; counts ATT opcode headers for unfragmented traffic, does NOT retain payloads; should not be mistaken for complete protocol decoding.
- Optional local face image metadata inspection: pixel width/height, MIME and file size; no conversion or device transfer.
- User-initiated Android storage picker saves ZIP: README, device, environment, advertising, gatt, readable values, capture-summary, image metadata, APK reference, diagnostics and Markdown report. protocol-events.jsonl is present but empty until evidence and safe recorder exist.

## Privacy defaults
The exported ZIP omits device MAC addresses, raw advertisement/manufacturer payloads and unknown characteristic raw values by default. Standard benign-readable values are retained, except serial/system identifiers. The user may opt in to raw/private export via the visible toggle and is instructed to review before sharing publicly.
No vendor APK or HCI payload or original photo is embedded in the bundle. Any captured proprietary bytes should be handled as private evidence.

## What this cannot establish
Service discovery cannot discover silicon manufacturer, memory sizes, screen dimensions, firmware source, undocumented commands or watch-face transfer formats unless these are exposed in readable data. A normal Android app cannot read HiWatchPro's private GATT transactions. A second-stage evidence workflow must use exact APK static analysis and/or controlled approved HCI traces. Do not guess commands.

## Build instructions
CI: GitHub Actions > "Build WatchProbe diagnostic APK" > artifact "WatchProbe-0.1.0-debug-APK". The workflow runs Gradle 8.9, JDK 17, Android SDK 35, :app:testDebugUnitTest and :app:assembleDebug. Artifact contains app-debug.apk, a debug-signed APK for sideload testing, not the production companion.
Local if SDK/Gradle installed: gradle :app:testDebugUnitTest :app:assembleDebug.

## Mandatory hardware validation on OPPO Reno10 5G
1. Install debug APK from successful GitHub Actions artifact; launch. Confirm there is no Internet permission.
2. Grant Nearby Devices (required for BLE on Android 15); turn phone Bluetooth on.
3. With watch nearby, press "Scan" and select only the observed T700Pro Max.
4. Press "Inspect this device safely". Never approve a surprise pairing dialog; app should not request bonding.
5. Wait for "Read-only survey complete" or record reported error. Export ZIP with raw toggle OFF.
6. Inspect GATT service and characteristic table, READ_OK values and errors. Share only sanitized ZIP through a trusted channel.
7. If necessary, separately import an H4 snoop log or inspect a source face image; neither is required for the first successful scan.
8. No on-device validation was performed by the coding agent. Report all unexpected watch changes immediately; never test guessed BLE writes.


## Verified build metadata (2026-11-14)
CI run: https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43620616666 — SUCCESS (Gradle `testDebugUnitTest`, `assembleDebug`, uploaded artifact).
Artifact ID: 14124400066; file app-debug.apk; size 9,510,840 bytes; SHA-256 `f89e60bfa1d7b365e74d88e93fff7af106f59cfaf8b820d3909410acb5e7de72`.
The output APK was downloaded from GitHub, unzipped and checked for archive integrity. No emulator or physical device execution was performed.

## T700 Companion stage-one (2026-11-14)
A new independent application module at `companion/`, package `io.github.batmancpu.watchcompanion` is under development. Source offers concrete standard battery/device-information reads, connection management, opt-in CCCD-only notification observation (battery/known vendor channels), local privacy-first ZIP export, no Internet permission and no proprietary writes/firmware operations. Existing WatchProbe `app/` is retained for archival diagnosis. New CI builds/verifies both APKs. Refer to `docs/COMPANION_STAGE1.md` for safety gates and feature status; new companion on-watch installation/behavior has not yet been tested.
