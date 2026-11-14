# CURRENT TOP PRIORITY 2026-11-15: T700 Companion 0.3.0 feature lab

User wants substantial progress including time sync, calls, messages, health/watch faces, explicitly asks to test and implement. We inspected private `/mnt/data/T700-Companion-20261115-0109.zip` (1.5KB): watch is physically CONNECTED, GATT 8 services, battery 100, manufacturer LY716, firmware V09708, hardware 01, model MI RC. All 3 CCCD subscribe operations succeeded, and 2 events of 20 bytes were received on `6e400003-b5a3-f393-e0a9-e50e24dcca9d`; raw content OFF, zero vendor writes. Session report shows user repeatedly tapped scan while Bluetooth OFF and scan in progress; improve UX. Original HiWatchPro 1.3.79 SHA d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc is known from older notes but exact binary is NOT present in current container, conversation uploads or Files Library. Do NOT claim static analysis of its code in this session.

New v0.3.0 code: separate `PhoneNoticeLab.kt` with Android NotificationListenerService only for WhatsApp/SMS/Telegram/known dialers, manual system permission and opt-in switch, metadata counts only; `FaceLab.kt` selects/crops local image into user-sized offline PNG, no upload; `CompanionActivity.kt` new integrated test workbench with clock check and start/end feature experiments correlated against watch packets and phone notifications and ZIP report; `WatchBleClient.kt` scan debounce and repeated-connect safeguards. `companion/build.gradle.kts` version bump to 0.3.0, AndroidManifest registers listener; JUnit tests in FeatureLabTest.kt; GitHub Actions artifacts rename 0.3.0. Build/status **PENDING at source authoring**, verify CI and handle errors before delivering APK. No app-level BLE writes, time sync, forwarded messages, calls TO watch, health/steps, or faces transferred; require evidence. The previous v0.2.0 APK is CI verified and physically tested only via user's ZIP. See docs/FEATURE_ROADMAP.md for ordered comprehensive plan and exact v0.3 tests. WatchProbe retired from main, don't restore. Keep docs/PROJECT_STATE and docs/SESSION_LOG updated with each code commit.

---

# CURRENT PRIORITY (overrides older historical handoffs)

## 2026-11-15 — WatchProbe retired; companion only
**Current goal:** Maintain T700 Companion as the sole Android application. After the user's successful physical WatchProbe read-only survey, WatchProbe is no longer necessary on their phone. User explicitly requested its retirement/removal. The former `app/` module and its CI build/artifact have been removed from main; its original source remains recoverable only through Git history. Do not recreate WatchProbe or require another diagnostic installation unless new evidence makes that necessary and the user agrees. The **sanitized watch findings** (8 GATT service entries; 24 characteristics; LY716; V09708; MI RC; battery 100% at inspection; proprietary vendor notify/write endpoints) remain documented in `docs/DEVICE_PROFILE.md`. The private WatchProbe ZIP has not been committed. Future builds target only `:companion:testDebugUnitTest` and `:companion:assembleDebug`. Watch-specific advanced command support is still blocked by absent verified protocol traces/accessible original HiWatchPro APK; never send guessed vendor commands. Signing remains ephemeral debug signing until a permanent release key is securely configured. Physical T700 Companion v0.2.0 operation has not been verified by the agent. Repository cleanup itself cannot uninstall an app from the user's phone; user may remove WatchProbe through Android Settings > Apps.

## Historical continuation notes (do not interpret as current deployment instructions)
# NEXT AGENT PROMPT — Bluetooth-Devicefier / T700 Pro Max

You are inheriting this project from a previous AI agent. The user deliberately maintains project memory in this repository because chat context can expire and AI agents can change. **The repository is the source of truth.** Continue from the exact recorded state; do not restart the conversation or ask the user to repeat facts already documented.

## Mandatory reading order

Before any work, read:
1. `AGENTS.md`
2. This file, `NEXT_AGENT_PROMPT.md`
3. `docs/PROJECT_STATE.md`
4. `docs/DECISIONS.md`
5. `docs/DEVICE_PROFILE.md`
6. `docs/APK_REFERENCE.md` and `docs/EVIDENCE/APK_STORAGE.md`
7. `docs/RESEARCH_AND_REQUIREMENTS.md`
8. `docs/CONVERSATION_HANDOFF.md`
9. The latest entries in `docs/SESSION_LOG.md`

Then inspect the repository tree, current branch, git status and recent commits. Do not overwrite or discard user work.

## Project identity and user constraints

- Repository: `Batmancpu/Bluetooth-Devicefier`; primary branch: `main`.
- Target hardware: **T700 Pro Max / T700Pro Max** proprietary Bluetooth smartwatch.
- Original companion: **HiWatchPro 1.3.79** (supplied by the user).
- Phone: OPPO Reno10 5G, Android 15 / ColorOS 15, MediaTek Dimensity 7050.
- User has no USB/data cable for the watch; Bluetooth is the only practical connection.
- User emphatically does not want to brick, reset, or damage the watch.
- Diagnostic app working name: **WatchProbe** (provisional).
- User prefers direct execution, minimal unnecessary tooling, one comprehensive research application, and persistent repository handoff across agents.

## Immediate project objective

Build **one comprehensive, one-time Android diagnostic/research application** that collects the maximum useful information about the actual physical watch and the working HiWatchPro companion. This is NOT yet the final always-on companion.

The user has already used nRF Connect and supplied screenshots. Do not send them through a chain of unrelated scanner apps. The diagnostic tool should combine, in one guided workflow:
- advertising and manufacturer data;
- connection and BLE identity information;
- complete GATT services, characteristics, descriptors and properties;
- safe readable characteristics and device information;
- supported connection details / capabilities;
- protocol evidence and optional imported HCI snoop capture analysis;
- feature/protocol clues derived from static HiWatchPro APK analysis;
- watch-face image/template format, dimensions, processing, packetization, acknowledgements, limits and transfer behavior;
- structured export (JSON plus readable report and sanitized evidence).

The user expects a purposeful instrument, not just a scanner or attractive mock UI. Static APK analysis should guide which features and protocol paths the tool investigates. The goal is the fullest defensible technical picture of this specific watch, not an unsupported claim to know hidden hardware information.

## Long-term objective (later stage only)

After the actual watch protocol is understood, the user may want a privacy-first open-source replacement for HiWatchPro:
- no ads, telemetry, analytics or unnecessary permissions;
- WhatsApp and other app notifications;
- phone call notifications;
- health/activity data and watch settings where supported;
- custom/better watch-face support where possible;
- polished iOS 27-inspired Liquid Glass foreground UI;
- minimal, event-driven background engine.

The user's ambition of less than 1% phone battery consumption in 24 hours belongs to the future always-on companion. It is an optimization target to measure, not a guarantee and not a requirement for this one-shot diagnostic tool. Keep foreground UI independent from background BLE logic.

## Verified physical-watch evidence

User-supplied nRF Connect screenshots showed:
- Complete local name: `T700Pro Max`.
- BLE LE-only, legacy advertising.
- Flags displayed: LE Limited Discoverable and BR/EDR Not Supported.
- One RSSI sample around -80 dBm (not a fixed property).
- Manufacturer data shown with reserved company ID `0x344C`.
- Primary GATT services: `0x1800`, `0x1801`, `0x180A`, `0x180F`, `0x1812`, `0xFFFF`, and vendor UUID `6e400001-b5a3-f393-e0a9-e50e24dcca9d`.
- nRF Connect displayed NOT BONDED.
- Screenshots showed service entries only; characteristics/descriptors were not expanded.
- The screenshot showed a hardware address, intentionally excluded from public docs.

**Critical UUID warning:** the vendor UUID ends in `cca9d`, not `cca9e`. Do not call this Nordic UART or assume typical Nordic UART RX/TX characteristic UUIDs. Discover actual characteristic UUIDs/properties and actual traffic.

Chipset, firmware revision, full GATT database, protocol and watch-face format remain unknown.

## Original HiWatchPro APK

Exact user-supplied file:
- Name: `HiWatchPro.apk`
- Size: 111,842,623 bytes (106.66 MiB)
- SHA-256: `d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc`
- 4,623 ZIP entries; 13 DEX files.
- Archive/string observations: HiWatchPro 1.3.79, `xfkj.fitpro`, `com.legend.bluetooth.fitprolib`, plus Yichip, Telink and Realtek/RealSil DFU/OTA namespace evidence.
- These are bundled app observations, NOT proof of which chipset this watch uses.

### Two-part APK transfer prepared

The exact APK was split into two byte chunks in the prior ChatGPT session and both were reassembled and verified byte-for-byte. The chunks are provided as downloadable artifacts in that ChatGPT response; they are **not yet in this repository**.

- `HiWatchPro.apk.part01`: 55,921,312 bytes; SHA-256 `74e033ce802fdc5d00bea29bb8f500c571b2b543db59c9389e83c28a6a634ead`.
- `HiWatchPro.apk.part02`: 55,921,311 bytes; SHA-256 `3b2c352331e926644f9c5243d87a7aab9b665584b581e8d6ea6039fe33c41dbb`.

Original expected result after reassembly:
- 111,842,623 bytes
- SHA-256 `d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc`

When the parts are available:
1. Verify both exact byte counts and hashes.
2. Reassemble part01 then part02 with `tools/reassemble_HiWatchPro.py` (if present) or a verified equivalent.
3. Verify the reconstructed APK size and SHA-256 exactly match the original.
4. If any hash differs, stop APK analysis and request the correct source file.
5. Keep reconstructed APK in an ignored private workspace folder (e.g. `private-evidence/`).

**Privacy and GitHub upload constraint:** the repository was last observed as PUBLIC. Do not commit the APK or parts into public Git history or public releases. Do not change repository visibility yourself. The user must first make the repository private and explicitly authorize private storage before a future agent adds the parts. GitHub browser repository uploads are limited to 25 MiB per file; command-line Git accepts up to 100 MiB per file, so these ~53.33 MiB parts require an agent/CLI Git environment rather than browser upload. Official documentation: https://docs.github.com/en/repositories/working-with-files/managing-files/adding-a-file-to-a-repository

## Immediate work plan

### 1. Inspect the original APK first
Once the exact APK is available and hash-verified:
- Parse AndroidManifest.xml correctly.
- Establish package ID, version code/name, minSdk/targetSdk, permissions, services, receivers and exported components.
- Extract help/manual/privacy resources.
- Decompile DEX for focused static analysis.
- Separate app-owned code from third-party SDKs.
- Locate BLE scan/connect, device-family selection, service/characteristic UUIDs, command tables, encoders/decoders and transport code.
- Map message/notification, call, time, alarm, health and settings flows.
- Trace custom watch-face workflow end-to-end: photo selection, crop/resize, pixel format/compression/packing, metadata/template, chunking, acknowledgements and completion handling.
- Identify OTA code only to explicitly exclude it from implementation.

### 2. Design the one-app diagnostic workflow
Combine:
- BLE scanner and advertisement inspector;
- complete GATT database discovery;
- explicit safe-read pass;
- device information dashboard;
- protocol evidence recorder;
- optional HCI snoop file import/parser if feasible;
- local watch-face asset inspector;
- JSON + Markdown/HTML report export with sanitized raw evidence.

Do not claim a normal Android application can directly inspect another app's private GATT calls. If runtime traffic from HiWatchPro is needed, investigate Android Bluetooth HCI snoop capture and build/import a parser where feasible. Use dummy message content and a test face during captures. Do not turn this into an endless collection of third-party apps.

### 3. Safety gate
- Safe default: BLE scan, GATT discovery, and reads only where READ property is explicitly present.
- Do not write characteristics or descriptors during initial discovery.
- Notification/indication subscriptions configure CCCD and are state-changing; do not enable automatically.
- Any normal app-level write (including watch-face transfer) requires a verified protocol path, documented effect/reversibility/failure behavior and explicit user approval.
- Never send guessed packets.
- Firmware flashing, OTA/DFU, bootloader access/unlock, factory reset and undocumented destructive operations are out of scope.

### 4. Build
- Prefer native Kotlin + Android SDK + Jetpack Compose.
- Keep architecture straightforward and robust; avoid needless modules/dependencies.
- Use GitHub Actions to build a downloadable debug APK so the user can install on Android without a PC.
- No ads, analytics, telemetry, cloud account or unnecessary internet permission.
- Request only necessary Android permissions.
- A clean, premium UI is welcome; research reliability and safety take priority.
- Test build, GATT data model, parsers, JSON export, permissions and error handling. Distinguish automated testing from actual hardware validation.

## Evidence/confidence labels

Every claim must be labelled:
- **Verified on this physical watch**
- **Verified in the exact supplied APK**
- **Documented by a third party**
- **Hypothesis / candidate**
- **Unknown / not tested**

A UUID match or bundled SDK does not prove chipset compatibility. APK code presence does not prove that a branch runs on this model. Do not claim hardware testing unless the user has performed it and provided evidence.

## Mandatory continuity: update NEXT_AGENT_PROMPT.md on EVERY substantive commit

The user explicitly requires the next AI to maintain this prompt after each commit so a later AI inherits the exact state.

For **every commit** that changes code, configuration, tests, research, evidence, requirements, plans, scope or project state:
- update `NEXT_AGENT_PROMPT.md` in the **same commit**;
- update `docs/PROJECT_STATE.md` and `docs/SESSION_LOG.md` in the same commit;
- update `docs/DECISIONS.md` when a durable decision/goal changes;
- update relevant technical docs;
- update `AGENTS.md` if permanent operating rules change;
- update `README.md` if onboarding/status changes.

The prompt must state the current goal and any approved goal change, actual completed work, files/components changed, exact tests/builds run and results, verified findings and confidence, decisions, bugs/risks, blockers, next action, APK availability and current commit context.

Do not make a second commit solely to update the prompt. Include the prompt refresh with the substantive change to avoid a self-referential prompt-only commit loop. For a documentation-only commit, refresh the prompt and log where the state materially changes.

Before ending a session, read the updated docs back from GitHub and confirm they reflect the actual main branch. The repository, not chat memory, must allow the next agent to continue without asking the user to repeat context.

## Start now

1. Inspect current main, working tree and complete documentation.
2. Check whether the user attached both APK parts to this agent session or placed them in its workspace.
3. If present, hash-check and reconstruct before APK analysis. If absent, ask only for the two exact part files; do not ask the user to repeat project context.
4. Perform focused APK analysis before designing the final diagnostic app.
5. Build the research tool rather than stopping at a plan/mockup, while keeping risky operations blocked.
6. Update this prompt, project state and session log in the same commit as every substantive change.

Final user response must report only actual changes, commit hash, tests/builds and results, hardware operations, remaining blockers and the next action. Never claim the watch was changed if it was not.


## Continuation update — 2026-11-14: exact APK split prepared

The user asked that the original HiWatchPro APK be supplied in two parts. The exact file was split into:
- `HiWatchPro.apk.part01` — 55,921,312 bytes; SHA-256 `74e033ce802fdc5d00bea29bb8f500c571b2b543db59c9389e83c28a6a634ead`.
- `HiWatchPro.apk.part02` — 55,921,311 bytes; SHA-256 `3b2c352331e926644f9c5243d87a7aab9b665584b581e8d6ea6039fe33c41dbb`.

The two pieces were reassembled successfully and verified byte-for-byte against the original:
- Size: 111,842,623 bytes.
- SHA-256: `d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc`.

The part files are downloadable artifacts from the previous chat response; they are **not stored in GitHub**. The repository was last observed as public. Do not commit proprietary parts to public Git history. Do not change repository visibility. The user must make it private and authorize storage before parts can be committed. Each part is under Git's 100 MiB command-line limit but above GitHub's 25 MiB browser upload limit; use the agent's Git environment if private storage is approved. See `docs/EVIDENCE/APK_PARTS.md` once available.

A verified reassembly script and part manifest are being added. Keep the APK in ignored private workspace storage after reconstruction.


## Handoff status updated — 2026-11-14

The repository now includes:
- `docs/EVIDENCE/APK_PARTS.md` and `docs/EVIDENCE/APK_PARTS.json` with exact part hashes/sizes;
- `tools/reassemble_HiWatchPro.py` to reassemble the supplied two parts and verify the exact original APK;
- updated `AGENTS.md` making same-commit NEXT_AGENT_PROMPT maintenance mandatory.

The parts remain downloadable artifacts from the preceding ChatGPT response, not repository files. Reassembly was tested successfully and matched the original byte-for-byte. Repository visibility was last observed as public, so do not commit the proprietary parts. Ask the user only for the required privacy/attachment step: make the repository private if they want private in-repo storage, and attach both split files to the coding-agent session. Do not ask the user to repeat project history.


## 2026-11-14 handoff — WatchProbe Android implementation initiated
**Current approved goal:** One-time READ-ONLY diagnostic APK for the T700 Pro Max; final HiWatchPro replacement is a different stage.
**Code added:** Gradle multi-file Android Kotlin/Compose app at `app/`: `MainActivity.kt` (permissions, guided UI, local files, ZIP export), `BleSurvey.kt` (15-second BLE scans, manual connection, full service/characteristic/descriptor enumeration, unencrypted property-READ-only queue, timeouts), `BtsnoopParser.kt` (32 MiB-bounded H4 metadata-only ATT opcode counts), `ReportWriter.kt` (privacy-aware JSON + Markdown ZIP bundle), test `BtsnoopParserTest.kt`, `.github/workflows/android.yml` for debug APK.
**Risk gates:** No automatic bonding or encryption-required characteristic reads; no characteristic/descriptor writes, CCCD, notifications, guessed packets, face transfer, firmware access, OTA/DFU or reset. Scans and GATT reads are Bluetooth exchanges but should not persistently change device settings. Device selection is manual. Default local ZIP redacts MACs, raw advertising and non-standard characteristic payloads; raw option requires direct user action.
**Validation:** At time of initial code commit no Android SDK or Gradle build was available in the chat tool runtime; GitHub Actions build and unit tests are REQUIRED before claiming APK generated. Actual physical watch operations: NONE in coding session. Check GitHub Actions latest run and fix failures.
**Evidence:** Physical watch identity/UUIDs still based ONLY on prior nRF Connect screenshots. Vendor service ends in `cca9d`. Chipset and proprietary command meanings UNKNOWN. HiWatchPro.apk and exact two-part files NOT present in this current agent session, and NOT in the public repository. Do not fabricate static analysis or assume cached session files.
**Next action:** Check CI status; correct source and retest until APK artifact is available. User installs the debug APK on OPPO Android 15, grants Nearby Devices, scans/selects T700Pro Max, exports ZIP with sensitive toggle OFF; share a sanitized report. Later, privately obtain the fingerprint-matching HiWatchPro APK and analyze its relevant protocol routing; write evidence and uncertainty clearly.
**Continuity:** Every further substantive commit MUST update this file, docs/PROJECT_STATE.md, docs/SESSION_LOG.md together, plus relevant decision/technical docs; verify latest main before writing. The implementation commit is the single GitHub commit containing this section; check Git history for full hash.


### CI failure and fix — 2026-11-14
- First push-triggered Actions run: https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43620460480 FAILED during SDK setup, before compiling, because android-actions/setup-android@v3 attempted obsolete sdkmanager package `tools`.
- Workflow modified to find and invoke the GitHub runner's own SDK manager. Confirm the next run succeeds before advertising an APK. Source syntax/Android correctness has not yet been established by compilation; all Bluetooth hardware evidence remains prior screenshots only.


### WatchProbe 0.1.0 build CONFIRMED — 2026-11-14
**CI run:** https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43620616666 (`43620616666`) for source/workflow commit `7dff8fea6310ab9dbeb90b1ffca53de9e3e5c361` succeeded. `:app:testDebugUnitTest`, `:app:assembleDebug`, `actions/upload-artifact` all passed. Artifact `WatchProbe-0.1.0-debug-APK` (ID `14124400066`) contains `app-debug.apk`, 9,510,840 bytes, SHA-256 `f89e60bfa1d7b365e74d88e93fff7af106f59cfaf8b820d3909410acb5e7de72`.
**Hardware validation:** NEVER PERFORMED in this coding session; do not claim device-specific read success. User should install debug APK, grant Nearby Devices on OPPO Android 15, choose T700Pro Max manually, run GATT read-only survey, export ZIP with raw toggle OFF and provide report. Watch firmware/settings were not accessed or changed.
**Binary availability:** HiWatchPro reference APK / split parts STILL unavailable in this session, NOT in public repository. Protocol mapping, watch-face transfer and final companion remain unimplemented. Exact vendor service UUID ends in cca9d.
**Next action:** Get sanitized device report first; analyze and document what was actually read, then privately obtain fingerprint-matching original APK if deep reverse engineering is needed. Continue updating project docs plus this prompt in each substantive commit.

### Installation troubleshooting (2026-11-14)
User could not install first WatchProbe debug APK on their OPPO; installer text/screenshot was not included in this message. Existing shipped APK hash was confirmed against CI (f89e60bfa1d7b365e74d88e93fff7af106f59cfaf8b820d3909410acb5e7de72), archive test passed, V2 signature block was found, manifest app ID and permissions matched expectations. Added CI assertions for Android apksigner cryptographic verification, zipalign and aapt badging. Must confirm next GitHub Actions job success and present a verified APK if possible. If installation still fails, request the exact Android installer error/code and check sideload permission, wrong ZIP, installed package signature conflict, storage, and ColorOS restrictions before changing app code. Do not claim actual on-device install success or BLE hardware tests.

### Installer verification completed — 2026-11-14
GitHub Actions run https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43621720479 for f7df4d8e8d6548093ade63f4ad541814798d3cc6 PASSED Gradle unit tests, compilation, APK assembly, official Android apksigner verification, zipalign -c -p 4, aapt metadata dump and artifact upload. APK v0.1.0 debug has v2 signature true, v1/v3 false, package io.github.batmancpu.watchprobe, minSDK 26, targetSDK 35. Downloaded newly verified APK: 9,510,840 bytes, SHA-256 510895c0c2b2fb420d5b73ee376e01eeba25ad650fb3505802ac05b7ae9a493b. Watch hardware NOT accessed, user reports installation failure but the precise dialog remains unknown; do not claim this build installs until user tests it. UI workflow and BLE behavior not tested.

## IMMEDIATE HANDOFF: 2026-11-14 companion stage-one
User uploaded a private 5.2KB sanitized `WatchProbe-20261114-2317.zip`, verified 8 GATT services and 24 characteristic entries on physical T700Pro Max. Manufacturer string LY716, firmware V09708, hardware 01, model MI RC, battery 100% at read; exact vendor UART-ish service `6e400001-b5a3-f393-e0a9-e50e24dcca9d` with `…0002…cca9d` WRITE and `…0003…cca9d` NOTIFY; FFFF service FF22 WRITE_NO_RESPONSE and FF11 NOTIFY. ZIP has no proprietary packet bytes (raw OFF, protocol events empty, no snoop import). User wants functional APK now, without aesthetic work. New `companion` module version 0.2.0 and package io.github.batmancpu.watchcompanion is separate from WatchProbe, implements scan/connect/standard battery+identity read, manual CCCD-only temporary subscribe on strictly observed notify channels, private-by-default local ZIP. No vendor data writes, OTA, bonding, device setting changes, or guessed commands. Read docs/COMPANION_STAGE1.md and docs/DEVICE_PROFILE.md. Original HiWatchPro APK reference SHA is documented, but actual APK is NOT accessible in current conversation/Library/container; user may need to reattach privately. Never claim its code was inspected. Next actions: check Android CI, repair compilation or signing failures, download verified companion APK for user, and await physical test and capture. Update NEXT_AGENT_PROMPT, PROJECT_STATE, SESSION_LOG in every substantive commit. Do not claim advanced companion functions work without verified watch-specific protocol.

### First companion APK build complete
Actions run https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43623972393 (commit 554438e) SUCCESS, all tests, both builds, APK signature/zipalign/aapt. Companion artifact 14125256837 is a valid DEBUG-signed APK (9,494,460 bytes SHA256 8d8f6e160cd03d285009998876f84a4ebf2769a6efae59ad08789bb44f8c47a4), package io.github.batmancpu.watchcompanion. Physical watch is NOT tested with this module; user should install, scan, connect, check battery/identity, optionally tap CCCD notification observation, export ZIP with raw OFF. Original physical GATT ZIP enumerates **24 characteristics** (15 successful reads). Debug signer is ephemeral across CI runs, so do not claim upgrade-in-place compatibility. Original HiWatchPro APK bytes not accessible. No proprietary features implemented until protocol proven.

### 2026-11-15 v0.3.0 Kotlin compiler fix
Initial CI run 43634665772 FAILED at `companion:compileDebugKotlin` because `PhoneNoticeLab.enabled` generated setter and manually declared `setEnabled(Boolean)` had the same JVM signature. Changed method name to `setMonitoringActive(Boolean)` in service state object and Activity call sites. No hardware action. Re-running CI; don't claim passing APK before verifying.


### v0.3.0 release verification — 2026-11-15
GitHub Actions run 43634806135 (source commit 5086f0d5fff1b2942098c51514051cc6e829c8dc) completed SUCCESS: :companion:testDebugUnitTest, :companion:assembleDebug, Android apksigner v2, zipalign, aapt metadata. Package io.github.batmancpu.watchcompanion, version 0.3.0/versionCode 3, minSdk 26, targetSdk 35, label T700 Companion. Artifact ID 14130235385, name T700-Companion-0.3.0-debug-APK. Extracted companion-debug.apk is 9,511,020 bytes; SHA-256 6c00c77fa0cf89e910e991d1c972a8a405e7eef4f537e53b74b60b4c616437a5. Both artifact ZIP and APK archive integrity passed. This is still a CI debug-signed APK, may require uninstall of previous CI-signed 0.2.0 due signing certificate mismatch. Actual v0.3.0 execution on watch/phone remains pending user verification. No developer-side watch operations performed.
