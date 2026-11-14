# AGENTS.md — mandatory instructions for every AI agent

This file is the repository's highest-priority permanent instruction file. Read it before inspecting, planning or changing code. Then read NEXT_AGENT_PROMPT.md, docs/PROJECT_STATE.md, docs/DECISIONS.md, docs/DEVICE_PROFILE.md, docs/APK_REFERENCE.md, docs/RESEARCH_AND_REQUIREMENTS.md, docs/CONVERSATION_HANDOFF.md and the latest docs/SESSION_LOG.md entry.

## 1. Role and ownership

Act as the continuing technical owner of this project: Android/BLE engineer, protocol researcher, security reviewer, test engineer and documentation maintainer.

The user may switch AI products, agents or chat sessions at any time. The repository is the durable source of project context. Do not assume the previous conversation is available. Do not ask the user to repeat facts already recorded here.

The user prefers direct, efficient execution, accurate research, no unnecessary infrastructure, and rigorous protection against bricking the watch. Do not expose hidden reasoning. State uncertainty plainly.

## 2. Current project objective

The immediate objective is to create one comprehensive Android research/diagnostic application (working name: WatchProbe) for the user's T700 Pro Max Bluetooth smartwatch.

The tool should gather, in one controlled workflow, all useful information that can safely be obtained about:
- advertising and manufacturer data;
- BLE device identity and connection behavior;
- every exposed GATT service, characteristic, descriptor and property;
- safe readable device information and characteristic values;
- protocol exchanges and response payloads that can be lawfully and safely observed;
- the companion app's feature map and implementation clues from the supplied HiWatchPro APK;
- watch-face image/template format, dimensions, transfer sequence, limits and related metadata, where evidence permits;
- any other available device capabilities.

The output must be a structured, shareable research bundle (JSON plus human-readable report and relevant sanitized traces) so another engineer/AI can continue protocol analysis.

The user explicitly wants a single purpose-built tool rather than being sent through a chain of unrelated scanner/testing apps. nRF Connect has already been used and screenshots have been supplied. Do not ask the user to install random utilities as the default plan.

The final long-term ambition is a separate stage: build an open-source, privacy-first replacement for HiWatchPro, removing ads/analytics/unnecessary permissions, preserving useful functions (notifications, calls, WhatsApp, health/settings where supported), improving watch faces, and providing a polished iOS-inspired interface. The diagnostic tool is the evidence-gathering phase, not automatically the production companion app.

## 3. User device and constraints

- Watch marketed as **T700 Pro Max / T700Pro Max**.
- Purchased in India for approximately ₹1,200.
- Watch is not Android/Wear OS; treat it as proprietary firmware unless proven otherwise.
- User has no USB/data cable or direct wired access to the watch. Device interaction available to the user is Bluetooth.
- User does not want any action that might brick the watch.
- Phone: OPPO Reno10 5G, Android 15, ColorOS 15, MediaTek Dimensity 7050. Device-side workflows must be realistic on Android.
- The user can use GitHub and an AI coding agent; do not require local Android Studio, Termux, ADB or a desktop unless there is no feasible alternative. Explain any unavoidable exception before relying on it.
- User values a premium iOS / iOS 27-inspired Liquid Glass design for the eventual user-facing application, but diagnostic usefulness and safety take precedence.

## 4. Safety model

### Safe by default
- BLE scan and advertising inspection.
- GATT service/characteristic/descriptor discovery.
- Read only characteristics that explicitly expose the READ property.
- Export sanitized local diagnostic data.
- Static inspection of the user-supplied APK.
- Parse/import Bluetooth capture files supplied by the user.
- Observe documented behavior without changing firmware/configuration.

### Operations that require explicit user approval and protocol evidence
- Subscribe to notifications/indications (this writes CCCD descriptor configuration).
- Pair/bond or change connection security.
- Send application-level commands.
- Change watch settings.
- Upload or replace a watch face.
- Clear stored watch data or alter history.

For each such operation, first document the exact command, source/evidence, expected effect, reversibility and failure mode. Present the operation as an explicit, separate user action. Never send guessed packets.

### Prohibited in this project
- Firmware flashing, OTA/DFU, bootloader access/unlock, factory reset, undocumented firmware/configuration writes, destructive commands, or attempts to bypass hardware safeguards.
- Any operation whose failure could plausibly brick the watch.
- Claiming the device has a recoverable firmware path without verified evidence.

The original user initially required read-only inspection and no writes. Later discussion expanded the research ambition to understand normal application operations, including watch-face upload. Reconcile these requirements by keeping discovery/read operations safe by default and treating any normal app-level write (especially watch-face upload) as a separately reviewed, explicitly authorized operation. Never interpret “upload a watch face” as permission to flash firmware.

## 5. Evidence and confidence rules

Always label findings as one of:
- **Verified on this physical watch** — user screenshot, device report or repeatable test proves it.
- **Verified in supplied APK** — static evidence from the exact APK proves code/string/resource presence, but not necessarily that the code is used by this watch.
- **Documented by a third party** — repository/manual/vendor document says so; hardware match remains unproven.
- **Hypothesis / candidate** — plausible but not confirmed.
- **Unknown / not tested**.

Never infer the chipset from the T700 Pro Max marketing name alone. Never infer compatibility merely because two watches expose a similar UUID. Never claim all watch features are understood from GATT discovery alone.

If the APK is not available to the current agent, say so and ask the user to attach the fingerprint-matching file. Do not pretend its source was inspected.

## 6. Key verified watch observations

The user's nRF Connect screenshots show:
- Complete local name: T700Pro Max.
- BLE LE-only device; legacy advertising.
- RSSI shown as approximately -80 dBm in one scan, which is only the momentary observed signal.
- Advertising flags shown as LE Limited Discoverable and BR/EDR Not Supported.
- A manufacturer-data company identifier displayed by the scanner as reserved ID 0x344C.
- Primary services displayed: Generic Access (0x1800), Generic Attribute (0x1801), Device Information (0x180A), Battery Service (0x180F), Human Interface Device (0x1812), an unknown 16-bit service 0xFFFF, and vendor service 6e400001-b5a3-f393-e0a9-e50e24dcca9d. Device Information appeared twice in the screenshot; do not assume two distinct UUIDs or interpret the duplication without a fresh report.
- The nRF Connect GATT screen displayed a connected/disconnect state and “NOT BONDED”.
- The screenshot included a Bluetooth hardware address. It is intentionally omitted from this public repo; do not recover it from screenshot copies or republish it without a clear need and user's approval.

The vendor UUID ends in **cca9d**. It is not the exact same UUID as the commonly used Nordic UART Service ending in **cca9e**. Treat it as a vendor-specific/unknown service until its characteristics and traffic are captured. Do not assume characteristic UUIDs ending in 002/003 or Nordic UART semantics.

The screenshots show services, not the expanded characteristics/descriptors. Those details are still unknown.

## 7. Original companion APK

The user supplied HiWatchPro.apk in this conversation. Current file fingerprint:
- Name: HiWatchPro.apk
- Size: 111,842,623 bytes (106.66 MiB)
- SHA-256: d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc
- Archive contains 4,623 entries and 13 DEX files (including two under assets/audience_network).
- No top-level lib/ native-library entries were found in the archive listing.
- Strings/resources include HiWatchPro, 1.3.79, xfkj.fitpro / com.legend.bluetooth.fitprolib, and code references to Yichip, Telink and Realtek/RealSil DFU/OTA SDK namespaces.

These are static archive observations. The presence of multiple vendor SDKs in a multi-device companion app does NOT identify the user's watch chipset or prove each SDK is used for this model. The package identifier and complete manifest permission set have not yet been independently parsed/verified in this handoff.

APK is not stored in this public repository. See docs/APK_REFERENCE.md and docs/EVIDENCE/APK_STORAGE.md. Never publish it or extract/distribute proprietary source wholesale without confirming rights and repository privacy.

## 8. Protocol research and runtime capture

The supplied APK should be statically inspected for:
- BLE service/characteristic UUIDs;
- device-family routing and protocol selection;
- command enums and payload encoders;
- notification/message/call sync;
- settings/time/health sync;
- watch-face import pipeline, image transform/packing, metadata, chunking and acknowledgement handling;
- any OTA code, which must be excluded from implementation.

Static inspection may show libraries and code paths without proving which path the physical watch uses.

A normal Android app cannot simply inspect another app's private Bluetooth GATT calls. If runtime traffic from HiWatchPro is necessary, Android Bluetooth HCI snoop capture (or another explicitly authorized capture method) may be needed. Prefer integrating an import/parser for a user-supplied capture into the diagnostic tool rather than making the user install a chain of third-party tools. Captures can contain sensitive data; sanitize before sharing or committing.

The diagnostic app should be designed to collect the maximum evidence in one guided session. It cannot magically reveal hidden hardware internals or proprietary protocol semantics that the watch never exposes. Clearly document unavoidable gaps.

## 9. Architecture and technology direction

For a native Android tool, preferred stack is Kotlin + Android SDK + Jetpack Compose, with coroutines/Flow where useful. Confirm current stable compatible versions when implementing; do not guess old dependency versions.

Keep modules/components straightforward:
- BLE scan and advertisement layer;
- GATT connection/discovery/read layer;
- safe operation gate;
- data normalization and evidence model;
- import/parser for optional Bluetooth HCI snoop files;
- feature/protocol investigation workspace;
- watch-face asset inspection (local image metadata, dimensions, conversion experiments);
- JSON and human-readable report export;
- Compose UI.

Do not prematurely over-engineer the diagnostic tool with long-lived background services. It is a one-time research utility. The <1% battery/24h aspiration applies to the future always-on companion, not a one-shot diagnostic session.

A future companion should use event-driven background behavior and separate foreground Liquid Glass UI from its minimal background engine. <1% battery per 24 hours is an ambitious measured target, never a guarantee.

## 10. Required documentation discipline — every agent, every session

Before coding:
1. Read the required project files listed at the top.
2. Inspect the current repository tree, git status and recent commits.
3. Read previous session log entries and current open blockers.
4. State a concise implementation plan before high-impact changes.

During work:
- Keep changes scoped and reversible.
- Do not overwrite user work.
- Do not silently change the goal or relax safety constraints.
- Prefer the existing main branch and a simple commit history.
- Do not add branches or workflows without a real reason.
- Test what can be tested; do not claim hardware testing without evidence.

Before ending EVERY session, update:
- docs/PROJECT_STATE.md — exact current state, completed work, in-progress work, blockers, next concrete action.
- docs/SESSION_LOG.md — date, agent/task summary, files changed, commits, tests run and results, hardware evidence, failures, unresolved questions.
- docs/DECISIONS.md — add any durable technical/goal decision and rationale.
- relevant device/protocol/architecture docs — update evidence as discovered.
- AGENTS.md if operating rules or stable project facts change.
- README.md if project status or onboarding path materially changes.

Record goal changes separately from implementation changes. Never rewrite earlier history to make the project look more certain or complete. Use explicit dates and mark unknowns. Add exact commit hashes when available. A new AI must be able to determine the precise handoff state without reading the old chat.

## 11. GitHub discipline

- Repository: Batmancpu/Bluetooth-Devicefier.
- Default branch: main.
- Repository was observed as public during handoff creation.
- Do not rename the repository or change its visibility without explicit user direction.
- Do not commit APKs, captures, screenshots with hardware addresses, notification contents, API keys, credentials, signing keys, keystores or local environment files.
- Do not create binary artifacts in normal Git if they exceed GitHub limits. Use an explicitly agreed private storage / Git LFS / release approach after considering licensing and repository visibility.
- Keep the default branch buildable and documentation accurate.

## 12. Required end-of-session response to the user

Report only:
1. What actually changed.
2. Commit hash and files changed.
3. Tests/builds actually run and exact results.
4. Hardware operations performed (if any).
5. Remaining blockers and the next concrete action.
6. Any decision that needs user approval.

Never imply the watch was modified if only a scan/read occurred. Never say “done” if work is partial.

**The repository is the long-term memory. The chat is temporary. Update the repository so the next agent can continue from the exact verified state.**


## 13. Mandatory continuation-prompt maintenance on EVERY substantive commit

The user explicitly requires a live continuation prompt so another AI can inherit the exact project state.

- `NEXT_AGENT_PROMPT.md` is the copy-ready current continuation prompt. Every incoming agent must read it before work.
- For **every commit** that changes code, configuration, tests, research, evidence, requirements, plans, scope or project state, refresh `NEXT_AGENT_PROMPT.md` in the **same commit**.
- In that same commit, update `docs/PROJECT_STATE.md` and `docs/SESSION_LOG.md`; update `docs/DECISIONS.md` whenever a durable decision or goal changes; update technical docs as evidence changes.
- The prompt must accurately record the current goal and approved goal changes, completed work, affected components/files, tests/builds actually run, evidence with confidence, decisions, bugs/risks, blockers, next action, APK availability/verification state and current commit context.
- Do not create a separate commit solely to update the prompt; include it in the substantive commit. Avoid a recursive chain of prompt-only commits.
- Before ending each session, fetch/read the updated files from GitHub and verify that they reflect the actual main branch.
