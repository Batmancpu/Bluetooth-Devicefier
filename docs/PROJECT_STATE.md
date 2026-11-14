# Project state — canonical handoff

**Last consolidated:** 2026-11-14 (Asia/Kolkata)  
**Repository:** Batmancpu/Bluetooth-Devicefier  
**Primary branch:** main  
**Working diagnostic app name:** WatchProbe (provisional)

## 1. Current objective

Build one comprehensive, one-time Android diagnostic/research application for the user's T700 Pro Max smartwatch. It should collect the maximum useful technical information in one guided workflow: hardware identity clues, advertising, GATT structure, safe readable attributes, capability/feature evidence, protocol traces or imported captures, and watch-face format/transfer evidence. It should export a structured report and evidence bundle that an AI engineer can use to implement a privacy-first replacement companion later.

The user does not want to be sent through multiple unrelated scanner apps. They have already used nRF Connect and supplied screenshots. The original HiWatchPro APK is available as a user attachment in the current chat, but it is not committed to this public repository.

## 2. Long-term ambition (separate stage)

After the diagnostic findings are reliable, the user may want an open-source replacement for HiWatchPro:
- no ads, telemetry, unnecessary SDKs or excessive permissions;
- Bluetooth connection and synchronization;
- message and app notification mirroring, especially WhatsApp;
- call notifications;
- health/activity and device settings where supported;
- watch-face upload and improved/custom faces where technically possible;
- polished iOS 27-inspired Liquid Glass UI;
- very efficient event-driven background service.

This is not the current deliverable. Do not start production companion implementation until the user explicitly changes scope.

The <1% phone battery per 24-hour ambition belongs to the eventual always-on companion, not the one-time diagnostic app. It is an optimization target, not a guaranteed result.

## 3. Current repository state

At handoff:
- The repository is newly created, public, and uses main as default branch.
- It originally contained only a short README.
- No Android source code, app module, Gradle project, CI build or APK has been implemented.
- This handoff commit adds documentation, evidence summaries and agent rules only.

## 4. User environment and constraints

- Watch: T700 Pro Max / T700Pro Max, purchased around ₹1,200 in India.
- Watch is not Android/Wear OS.
- Only practical watch-to-phone interface available to user is Bluetooth; no USB/data cable.
- User does not want to brick or otherwise damage the watch.
- Phone: OPPO Reno10 5G, Android 15 / ColorOS 15, Dimensity 7050.
- User can operate Android and GitHub; avoid requiring desktop, ADB, Termux or local Android Studio unless unavoidable and explained.
- User prefers a single well-designed diagnostic application to gather the evidence, not an endless sequence of ad hoc tools.
- Premium Liquid Glass style is desired for the eventual polished UI. The diagnostic tool should be clean and efficient; don't let aesthetics undermine functionality.

## 5. Verified physical-watch evidence so far

From user-supplied nRF Connect screenshots:
- Advertised local name: T700Pro Max.
- BLE device type: LE only.
- Advertising type: Legacy.
- Flags: LE Limited Discoverable; BR/EDR Not Supported.
- One RSSI sample: approximately -80 dBm; not a stable characteristic.
- Scanner displayed a reserved company identifier 0x344C in manufacturer data.
- GATT services shown:
  - Generic Access — 0x1800
  - Generic Attribute — 0x1801
  - Device Information — 0x180A
  - Battery Service — 0x180F
  - Vendor/unknown service — 6e400001-b5a3-f393-e0a9-e50e24dcca9d
  - Unknown 16-bit service — 0xFFFF
  - Human Interface Device — 0x1812
  - Device Information 0x180A appeared a second time in the screenshot.
- The GATT screen displayed connected/disconnect and NOT BONDED.
- The first image shows primary services only; no characteristic list or descriptors were expanded.
- A Bluetooth MAC was visible in the original screenshot; it is redacted from repository docs because this repo is public.

**Critical:** the vendor UUID ends in cca9d, not cca9e. Do not assume Nordic UART semantics or known characteristic UUIDs. The service's characteristics, descriptors and packet format remain unknown.

## 6. Original HiWatchPro APK

The user supplied /mnt/data/HiWatchPro.apk in the originating ChatGPT session. The exact file is fingerprinted in docs/APK_REFERENCE.md.

Verified archive-level facts:
- 111,842,623 bytes (106.66 MiB).
- SHA-256 d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc.
- 4,623 archive entries.
- 13 DEX files, including the two under assets/audience_network.
- No top-level lib/ entries in the archive listing.
- Strings/resources include HiWatchPro, 1.3.79, xfkj.fitpro and com.legend.bluetooth.fitprolib, plus Yichip, Telink and Realtek/RealSil DFU/OTA SDK namespace strings.

These observations do not prove the T700's chipset. The app may bundle code for multiple models. Package ID and manifest details still need proper parser-based verification.

APK omitted from this public repository because it is 106.66 MiB (GitHub blocks regular Git files above 100 MiB) and is a proprietary app binary. Do not use Git LFS or other delivery to publish it publicly without user's explicit rights/authorization and a privacy decision. The user can attach the exact file privately to an AI agent session; verify SHA-256 before relying on it.

## 7. Current status by workstream

| Workstream | Status |
|---|---|
| Repository handoff docs | Being established |
| Physical watch identification | Partial |
| BLE advertising details | Partial, screenshot only |
| Full GATT characteristics/descriptors | Not collected |
| Safe Device Information reads | Not collected |
| Chipset identification | Unknown |
| HiWatchPro static reverse engineering | Basic archive/string inspection only |
| HiWatchPro feature map | Not completed |
| Runtime protocol capture | Not collected |
| Message/call protocol | Unknown |
| Watch-face file format/protocol | Unknown; original app allows photo/album-based custom face workflow per prior discussion, exact implementation unverified |
| Diagnostic Android app | Not implemented |
| Final companion app | Not started |
| Battery benchmark | Not applicable yet |

## 8. Critical next actions

1. Verify this documentation commit exists on main and read it from GitHub.
2. Have the next coding agent inspect the exact APK after the user provides it to that agent; parse manifest/package/version, inspect resources and decompile relevant application code.
3. Identify device-family routing and protocol selection inside HiWatchPro; distinguish bundled SDKs from the code path selected for this watch.
4. Design the one-shot diagnostic app's data model and guided flow before coding.
5. Ensure it can inspect BLE advertising/GATT, read supported information, import optional Bluetooth HCI snoop captures, analyse local image metadata and export a comprehensive report.
6. Keep all protocol writes disabled until individually understood and approved. Watch-face transfer may eventually be a reversible app-level operation, but do not send guessed payloads.
7. Build and test through GitHub Actions; device testing must be labelled as not performed until the user reports results.

## 9. Open questions

- What chipset is physically inside this specific T700 Pro Max?
- What characteristic UUIDs and descriptors are beneath the vendor and 0xFFFF services?
- Are any useful characteristics readable without bonding?
- What device information values does 0x180A expose?
- Which HiWatchPro device-family branch does this watch select?
- What is the exact notification/call/health synchronization protocol?
- What image dimensions, formats, packing, metadata and packet/acknowledgement protocol does watch-face upload use?
- Can a Bluetooth HCI snoop log be conveniently exported from the user's OPPO/ColorOS build without a PC?
- Does the device accept custom faces beyond the vendor app's templates?

Do not silently fill these gaps. Record evidence and confidence as it becomes available.


## 10. Update — two-part APK preparation and next-agent prompt (2026-11-14)

- The original HiWatchPro APK was split into two byte-exact parts for private transfer. Sizes and SHA-256 values are documented in `docs/EVIDENCE/APK_PARTS.md` and `docs/EVIDENCE/APK_PARTS.json`.
- Both parts were reassembled in the working environment; the output matched the original size (111,842,623 bytes), SHA-256 (`d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc`) and passed byte-for-byte comparison.
- The split part files are downloadable ChatGPT artifacts, **not committed to this GitHub repository**. The repository was last observed as public. Do not add the proprietary binary or parts to public Git history. The user must make the repository private and authorize private storage before a future coding agent adds the parts.
- Each chunk is about 53.33 MiB. GitHub browser uploads have a 25 MiB per-file limit; command-line Git allows up to 100 MiB per file. These chunks therefore require a Git CLI/agent environment, not browser upload.
- Added `NEXT_AGENT_PROMPT.md` as the canonical copy-ready prompt for the next AI. `AGENTS.md` now requires refreshing it in the same commit as every substantive change, alongside project state/session log updates.
- Added `docs/EVIDENCE/APK_PARTS.md`, `docs/EVIDENCE/APK_PARTS.json` and `tools/reassemble_HiWatchPro.py`.
- No Android diagnostic app has been implemented; no Bluetooth or firmware operation was performed.


## 11. Implementation update — 2026-11-14, WatchProbe 0.1.0
- First native Android Kotlin/Jetpack Compose project added under `app/`, with Gradle 8.9 / AGP 8.7.3 / Kotlin 2.0.21 and GitHub Actions debug APK builder.
- Phone-only workflow: permission-gated BLE discovery, manual watch selection, GATT service/characteristic/descriptor enumeration, READ-only unencrypted characteristic reads, local status and evidence.
- Optional local metadata analysis for uncompressed H4 btsnoop (no captured payload retained/exported), and local image bounds inspection (no watch-face upload).
- ZIP research export includes environment, device, advertising, GATT, safe readable values, imported capture summary, image input dimensions, APK reference, diagnostics, readable Markdown report and privacy notes.
- Default ZIP redacts Bluetooth addresses, raw advertising/manufacturer bytes and opaque characteristic values. User may explicitly enable local raw export and must inspect before sharing.
- No Android binary or real-device test had been verified when this code was first committed; GitHub Actions must establish build status. No watch operations were performed by this agent.
- The original HiWatchPro binary or split parts were not present in the current session; static reverse engineering is a remaining blocker, but does not block baseline GATT diagnostics.
- Next: verify CI compile/unit tests and artifact, fix failures if any, install/test on OPPO Reno10 5G and export the first watch report. Then use verified report and privately provided exact APK/captures to map watch-specific protocol.


### CI repair 1 — 2026-11-14
- Initial GitHub Actions run 43620460480 failed before compilation in android-actions/setup-android@v3 because sdkmanager rejected obsolete `tools` package. This was workflow infrastructure, not a Kotlin compile result.
- Replaced setup-android action with direct runner command-line SDK installer. Re-run triggered on main by workflow change; results must be verified independently.


### Verified CI artifact — 2026-11-14
- First CI run 43620460480 failed only during SDK setup. Follow-up run [43620616666](https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43620616666) on commit `7dff8fea6310ab9dbeb90b1ffca53de9e3e5c361` completed SUCCESS.
- Gradle ran `:app:testDebugUnitTest`, `:app:assembleDebug`, plus APK upload. Build was successful with 41 actionable tasks, 41 executed.
- Artifact name: `WatchProbe-0.1.0-debug-APK`; artifact ID `14124400066`. Contains `app-debug.apk` (9,510,840 bytes), SHA-256 `f89e60bfa1d7b365e74d88e93fff7af106f59cfaf8b820d3909410acb5e7de72`. Downloaded and verified ZIP integrity and inner APK ZIP structure. Artifact retention: 30 days per workflow.
- This proves build/packaging and local unit-test execution, NOT that Bluetooth scanning, GATT reads, permission UI or watch safety have been tested on the physical watch.
- No physical watch operations were performed. User should install APK, run scan/read, and provide REDACTED ZIP for evidence-based next phase.

### Installation failure report — 2026-11-14
User reported that WatchProbe 0.1.0 APK would not install on their phone. No exact Android error code or screenshot was supplied in this message. Downloaded previous APK `WatchProbe-v0.1.0-read-only-debug.apk` was checked locally: SHA-256 matches the CI report (`f89e60bfa1d7b365e74d88e93fff7af106f59cfaf8b820d3909410acb5e7de72`), ZIP archive integrity passes, package ID appears as `io.github.batmancpu.watchprobe`, and APK Signing Block v2 record exists. This does not prove device acceptance. The CI workflow was augmented with `apksigner verify`, `zipalign -c`, and `aapt dump badging` to validate output before publishing future artifacts. Await CI results and exact installer message to diagnose ColorOS restriction, source permission, package conflict, or file-transfer issue. Do not claim the problem is fixed until installed successfully.

### Installation validation result — 2026-11-14
Official verification CI run 43621720479 SUCCESS: APK compiles and passes unit tests; apksigner verifies valid v2 signature, zipalign passes, aapt reports package io.github.batmancpu.watchprobe, minSdk 26, targetSdk 35. New APK artifact ID 14124885541 from run; downloaded APK 9,510,840 bytes, SHA-256 510895c0c2b2fb420d5b73ee376e01eeba25ad650fb3505802ac05b7ae9a493b, archive integrity passes. This does NOT prove installability on OPPO ColorOS 15. Next action requires the exact system install error if failure persists and consideration of permissions/installer-specific policy and package certificate conflicts. No watch hardware accessed.

## 2026-11-14 — Companion development started from physical WatchProbe export
User uploaded private sanitized report `WatchProbe-20261114-2317.zip`; inspected its 12 files. Confirmed successful scan/connect/read, 8 GATT service entries, 24 characteristic entries, manufacturer LY716, firmware V09708, model MI RC, hardware 01, 100% battery at snapshot, vendor CC(C)A9D and FFFF write/notify endpoints. Private raw data OFF, protocol-events.jsonl empty, snoop imported=false. Report does not provide proprietary packets or opcodes, nor proof of chipset. Never commit raw ZIP.
User explicitly requested to begin a functional companion APK and defer aesthetics. Created separate `companion` module, version 0.2.0 and package io.github.batmancpu.watchcompanion. Implements T700 scan/manual connect, standard battery and device-info READ, explicit optional approved CCCD-only passive subscription on battery/…CCA9D/003/FFFF/FF11, local privacy-first report. No app-level writes, no fake UI for unsupported functions. Updated Actions to build/test/signature-verify both modules; build status and physical companion test still pending at authoring. See docs/COMPANION_STAGE1.md. Original HiWatchPro APK's SHA/reference exist, but its binary is not in current accessible conversation/Library/container.

### T700 Companion 0.2.0 build verified
GitHub Actions run https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43623972393 (source commit 554438ea7c03b0c14bfafd0ff9b88b78c5f020de) PASSED: both modules unit tested and assembled, official Android apksigner v2 verify, zipalign -c and aapt badging. Companion APK is package io.github.batmancpu.watchcompanion, minSdk 26, targetSdk 35, version 0.2.0; artifact ID 14125256837. Downloaded and locally verified ZIP and APK archive integrity. APK size 9,494,460 bytes, SHA256 8d8f6e160cd03d285009998876f84a4ebf2769a6efae59ad08789bb44f8c47a4. This is DEBUG-signed; future builds may have different signing identity and require uninstall before installation, until stable signing setup. There is no physical-phone/watch test of companion yet. WatchProbe physical export had 24 characteristic entries (15 READ_OK, 5 not readable, 1 SecurityException, 3 read start failures).


## 2026-11-15 — WatchProbe retired; companion only
**Current goal:** Maintain T700 Companion as the sole Android application. After the user's successful physical WatchProbe read-only survey, WatchProbe is no longer necessary on their phone. User explicitly requested its retirement/removal. The former `app/` module and its CI build/artifact have been removed from main; its original source remains recoverable only through Git history. Do not recreate WatchProbe or require another diagnostic installation unless new evidence makes that necessary and the user agrees. The **sanitized watch findings** (8 GATT service entries; 24 characteristics; LY716; V09708; MI RC; battery 100% at inspection; proprietary vendor notify/write endpoints) remain documented in `docs/DEVICE_PROFILE.md`. The private WatchProbe ZIP has not been committed. Future builds target only `:companion:testDebugUnitTest` and `:companion:assembleDebug`. Watch-specific advanced command support is still blocked by absent verified protocol traces/accessible original HiWatchPro APK; never send guessed vendor commands. Signing remains ephemeral debug signing until a permanent release key is securely configured. Physical T700 Companion v0.2.0 operation has not been verified by the agent. Repository cleanup itself cannot uninstall an app from the user's phone; user may remove WatchProbe through Android Settings > Apps.
**Cleanup verification:** The new companion-only GitHub Actions run is triggered by the workflow/configuration change; record its outcome after execution. Existing verified companion v0.2.0 artifact remains from run 43623972393. No watch-side operations were performed during repository cleanup.

## 2026-11-15 — v0.2.0 hardware evidence and v0.3.0 functional test build
Uploaded user file `T700-Companion-20261115-0109.zip` was read. `companion-state.json` states connected, 8 services, 100% battery, manufacturer LY716, firmware V09708, hardware 01, model MI RC. Three notification CCCDs enabled; exactly 2 proprietary vendor notification packets, 20 bytes each, from channel 6e400003-...-cca9d. `private_watch_payloads_included:false` and `vendor_write_commands_sent:0`; no command semantics can be decoded from packet lengths alone. `session-log.json` shows multiple scans requested with Bluetooth OFF and repeated scan restarts.
Implemented Kotlin companion v0.3.0: Bluetooth-enable prompt, scan duplicate suppression, new phone notification observer opt-in NotificationListenerService that records only allowlisted app class / event kind / timestamp (no content), guided experiment window correlation with watch-origin messages, offline selected-image crop/PNG export, clock check, expanded ZIP export, unit tests. Only standard BLE CCCD writes remain; no user-proprietary time/messages/call/health/faces commands sent. Build and physical testing pending CI at authoring. The original fingerprinted HiWatchPro APK binary could not be found by container or Files conversation/Library search; protocol command extraction requires exact APK bytes. See docs/FEATURE_ROADMAP.md. WatchProbe retired.

### 2026-11-15 v0.3.0 Kotlin compiler fix
Initial CI run 43634665772 FAILED at `companion:compileDebugKotlin` because `PhoneNoticeLab.enabled` generated setter and manually declared `setEnabled(Boolean)` had the same JVM signature. Changed method name to `setMonitoringActive(Boolean)` in service state object and Activity call sites. No hardware action. Re-running CI; don't claim passing APK before verifying.
