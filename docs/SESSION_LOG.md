# Session log

Append a new dated section at the end of every work session. Do not replace earlier entries. Record facts, not intended work.

## 2026-11-14 — Initial repository handoff consolidation

**Agent/task:** Consolidate the smartwatch project context into the user's newly created GitHub repository so future AI agents can resume after chat context changes.

### Repository inspected
- Repository: Batmancpu/Bluetooth-Devicefier
- Visibility observed: public
- Default branch: main
- Starting tree: only README.md
- Starting main commit: 5c2e514eea618db6af7d3df75ebe98e28e627b3a

### User/device facts preserved
- Target watch is marketed as T700 Pro Max / T700Pro Max.
- User has Bluetooth-only practical access and no USB/data cable.
- User does not want to brick the watch.
- Phone is OPPO Reno10 5G, Android 15 / ColorOS 15.
- User intends one comprehensive diagnostic tool first, then possibly an open-source replacement companion.
- Watch-face upload in HiWatchPro is an important research target.

### Screenshot evidence transcribed
The nRF Connect screenshots showed the advertised name, BLE LE-only/legacy advertising, flags, reserved manufacturer identifier 0x344C, and service UUIDs listed in docs/DEVICE_PROFILE.md. The hardware address was not copied into public documentation.

### APK fingerprinted
- Supplied file: HiWatchPro.apk
- Size: 111,842,623 bytes (106.66 MiB)
- SHA-256: d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc
- ZIP entries: 4,623
- DEX files: 13
- Archive string observations are in docs/APK_REFERENCE.md.
- APK was not committed because the repository is public, the file exceeds 100 MiB, and it is a proprietary third-party app binary.

### Repository changes in this consolidation
- Added durable agent instructions and lowercase agent.md entry point.
- Replaced the minimal README with a repository orientation page.
- Added project state, device profile, APK fingerprint, requirements/research plan, decision log, conversation handoff and evidence notes.
- Added a .gitignore that prevents accidental commits of APKs and private evidence.

### Tests and hardware actions
- No Android project exists yet, so no build/test was run.
- No BLE scan, connection, pairing, characteristic read or write was performed during this repository documentation task.
- No watch firmware/configuration was changed.
- No physical hardware operation was performed.

### Remaining blockers
- APK is not accessible to a future agent from the repository. User must attach the fingerprint-matching binary privately for static reverse engineering.
- Chipset, full characteristic database, readable values, firmware ID and actual protocol are unknown.
- No implementation has started.

### Next action
After verifying this handoff commit on main, the next agent should obtain the APK privately and perform focused static analysis before designing the comprehensive one-shot diagnostic app. Do not ask the user to install a sequence of third-party test apps.

**Handoff documentation commit:** `581ba62ad770e2420c838ad01c8a806eb7ec9c4b` (verified on `main`; see GitHub commit history).


## 2026-11-14 — Two-part APK preparation and continuation prompt

**Task:** User requested the exact original APK be split into two files and the next-agent prompt be maintained after every commit.

### Binary preparation
- Original: `HiWatchPro.apk`, 111,842,623 bytes.
- Original SHA-256: `d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc`.
- Part 01: 55,921,312 bytes; SHA-256 `74e033ce802fdc5d00bea29bb8f500c571b2b543db59c9389e83c28a6a634ead`.
- Part 02: 55,921,311 bytes; SHA-256 `3b2c352331e926644f9c5243d87a7aab9b665584b581e8d6ea6039fe33c41dbb`.
- Reassembly succeeded; expected output size/hash matched and `cmp` byte-for-byte test passed.
- Part files were prepared as downloadable attachments. They were not committed to GitHub because the repository is public, the APK is proprietary and this session's GitHub connector does not support uploading binary files or Git LFS assets.

### Repository changes
- Added `NEXT_AGENT_PROMPT.md`.
- Updated `AGENTS.md` to require same-commit prompt refresh for every substantive change.
- Added `docs/EVIDENCE/APK_PARTS.md`, `docs/EVIDENCE/APK_PARTS.json` and `tools/reassemble_HiWatchPro.py`.
- Updated `.gitignore` to keep local APK chunks and reconstructed APK in private workspace storage.
- The canonical project state now records the current APK availability and privacy blocker.

### Verification
- Reassembled APK: 111,842,623 bytes.
- Reassembled SHA-256: `d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc`.
- Byte-for-byte comparison against source: PASS.
- No Android build or physical-watch test was run.
- No BLE pairing, GATT write, notification subscription, firmware update or watch modification was performed.

### Next action
User should attach both part files to the next coding agent. If the parts are to be stored in GitHub, the user must first make the repository private and explicitly authorize it; the agent must use Git CLI because each part exceeds GitHub's 25 MiB browser-upload limit. Then reconstruct with `tools/reassemble_HiWatchPro.py`, verify the fingerprint and inspect the APK. Keep `NEXT_AGENT_PROMPT.md` current in the same commit as every substantive change.


## 2026-11-14 — WatchProbe first Android implementation
**Task:** Build safe, single-purpose diagnostic APK source directly in main with automated GitHub Actions packaging.

**Files:** `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/io/github/batmancpu/watchprobe/{MainActivity,BleSurvey,BtsnoopParser,ReportWriter}.kt`, `app/src/test/java/io/github/batmancpu/watchprobe/BtsnoopParserTest.kt`, `.github/workflows/android.yml`, `docs/IMPLEMENTATION.md`; updated README, DECISIONS, PROJECT_STATE, NEXT_AGENT_PROMPT and this log in the SAME commit.

**Safety:** No BLE hardware accessed by this coding session. App contains no characteristic/descriptor write, CCCD subscription, pairing request, OTA/DFU, factory reset or firmware update API. Local report export redacts identifiers/opaque payloads unless explicitly opted in.

**Validation at source-commit time:** Source review only; GitHub Actions compile, unit test and APK output pending. UI, permissions and physical watch not tested on hardware; do not claim success before CI proves it.

**Remaining:** HiWatchPro exact proprietary APK or split parts absent from this chat session; deep static protocol research pending. User must perform first on-device scan/read and supply sanitized report; maintain safety gates.

**Next action:** Verify GitHub Actions workflow, download APK only if the build succeeds, test on the specified OPPO handset, inspect exported ZIP without publishing device addresses.


### CI run 1 and workflow repair — 2026-11-14
- Run 43620460480 on main: FAILED at android-actions/setup-android@v3; underlying error `Warning: Failed to find package 'tools'` and exit 1. Compile/test/upload steps were skipped.
- Repair: remove incompatible Android setup action and invoke existing runner `sdkmanager` to install API 35/build-tools 35.0.0. CI rerun pending. No hardware accessed.


## 2026-11-14 — WatchProbe 0.1.0 CI VERIFIED
- Run [43620616666](https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43620616666) for commit `7dff8fea6310ab9dbeb90b1ffca53de9e3e5c361`: SUCCESS.
- Verified steps: SDK 35 install, `:app:testDebugUnitTest` PASS, `:app:assembleDebug` PASS, upload-artifact PASS. 41 Gradle tasks executed; Kotlin compilation successful.
- Artifact ID `14124400066`, name `WatchProbe-0.1.0-debug-APK`, internal filename `app-debug.apk`. Size 9,510,840 bytes; SHA-256 `f89e60bfa1d7b365e74d88e93fff7af106f59cfaf8b820d3909410acb5e7de72`.
- Downloaded artifact ZIP from GitHub through connected GitHub action, integrity-checked ZIP and APK archives, verified APK file type. Debug signing is for local development only. No physical watch operations performed.
- Remaining: validate app on OPPO Reno10 5G, inspect sanitized report, obtain exact HiWatchPro APK privately for additional reverse engineering. Do not treat successful compilation as successful device interoperability.

## 2026-11-14 — Android installation failure investigation
User reports "App not installed" without a readable screenshot or detailed Android installer code. Checked previously shipped file in container; APK SHA-256 matches recorded build, APK ZIP integrity clean, v2 APK Signature Block present (ID `0x7109871a`), and manifest contains expected application ID and Bluetooth permissions. Cannot conclude the failure cause from this alone; signer cryptographic verification and APK package compatibility check were added as mandatory CI steps. No Bluetooth device accessed or modified. Next: inspect CI verification and collect full installer dialog if failure persists.

## 2026-11-14 — APK install validation passed in CI
- GitHub Actions run 43621720479: successful Gradle tests and APK build; official Android apksigner (v2 true), zipalign and aapt badging all passed. Output has package io.github.batmancpu.watchprobe, minSdk 26, targetSdk 35. Artifact 14124885541.
- Verified artifact ZIP and extracted app-debug.apk locally (9,510,840 bytes), SHA-256 510895c0c2b2fb420d5b73ee376e01eeba25ad650fb3505802ac05b7ae9a493b. This is a new ephemeral debug signature, so may not replace any existing installation signed differently.
- User's precise Android installation dialog remains unavailable; no claim the issue is resolved, no watch or phone testing performed.

## 2026-11-14 — Physical report analyzed; companion stage-one source created
Inspected uploaded `WatchProbe-20261114-2317.zip` directly: validated 12 ZIP members including 8-services GATT inventory; device and standard READ results (LY716, V09708, 01, MI RC, 100%). Vendor write/notify channels found on exact …cca9d and FFFF endpoints; no watch-specific opcode captured. Source was sanitized, and raw/private data toggled OFF. Search of current accessible Files conversation and Library did not locate the original 111MB HiWatchPro APK, so did NOT static reverse-engineer its bytes or claim it is available.
Created a distinct `companion` module targeting Android minSdk 26: manual T700 scan/connect, battery + identity read, opt-in CCCD-only notification observation, bounded watch-only in-memory packet capture with opt-in raw export, and no vendor writes/no OTA/no reset/no cloud. Added ProtocolSafety gate and unit tests, CI build and Android signature validation for both APKs. See docs/COMPANION_STAGE1.md. Build and device validation pending.

## 2026-11-14 — Companion 0.2.0 CI success and exact APK delivery
GitHub Actions run 43623972393 succeeded (both Gradle tests/builds; Android apksigner v2, zipalign, aapt validation). Downloaded companion artifact ID 14125256837. `companion-debug.apk` size 9,494,460 bytes; SHA256 `8d8f6e160cd03d285009998876f84a4ebf2769a6efae59ad08789bb44f8c47a4`; unzip integrity PASS. Package id io.github.batmancpu.watchcompanion, minSdk 26, targetSdk 35. User install and watch connection NOT tested yet. Correction: original GATT report contains **24** characteristics, not the earlier mistyped 23 (15 READ_OK, 5 not readable, 4 read errors).


## 2026-11-15 — Retire WatchProbe source/module from active repository
User explicitly requested removing now-redundant WatchProbe after its physical watch survey was exported. Removed tracked `app/` Kotlin/Gradle module from active main branch, removed its `settings.gradle.kts` include, switched GitHub Actions to build/test/signature-verify/upload only `companion/`, and refreshed README, AGENTS, companion guide, decisions, handoff and project state. Archived code is accessible from previous Git commits; repository is not history-rewritten. Previously observed physical watch facts remain preserved in docs, no user ZIP/raw evidence committed. Hardware actions: none. User uninstall must be done on phone, not via GitHub. Build verification pending for the new companion-only CI run. The original HiWatchPro APK still not available for static opcode decoding; no guessed vendor commands allowed.

## 2026-11-15 — User-provided 0.2.0 live evidence and comprehensive 0.3.0 implementation
Inspected `T700-Companion-20261115-0109.zip`: connected T700, battery+identity correct, three CCCDs successfully enabled, 2 vendor notifications of 20 bytes, default privacy raw OFF; zero vendor writes. This proves live watch-origin BLE notification pathway and does NOT reveal commands. Checked container uploads, Files Library, and prior context for original HiWatchPro binary; only past metadata/fingerprint, not APK bytes. No static binary decompilation attempted or claimed. Implemented substantial 0.3.0 feature test release: notification source/call metadata-only listener with explicit Android system permission and separate on-app toggle; image crop/local PNG generator; guided named feature-test windows correlating watch/phone events; phone clock test; scan debouncing/Bluetooth enable prompt; expanded report and unit tests, manifest and gradle updates. See docs/FEATURE_ROADMAP.md for exact roadmap and testing procedure. No watch hardware commands were sent by agent. Build/CI status not yet verified at this entry.
