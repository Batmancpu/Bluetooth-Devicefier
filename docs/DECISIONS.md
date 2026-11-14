# Decisions and rationale

This file records durable choices. Add new decisions with date, status, rationale and consequences; do not silently rewrite prior decisions.

## 2026-11-14 — Current decisions

### D-001: One comprehensive diagnostic app, not a chain of scanner apps
**Status:** Accepted  
**Decision:** Build one purpose-built Android research tool that combines discovery, safe GATT inspection, evidence capture/import and report export.  
**Rationale:** The user already used nRF Connect and wants a single, purposeful app that collects the information needed for later development.  
**Consequence:** Do not ask the user to install several unrelated utilities as the default plan. Research existing libraries and APIs for implementation, but the deliverable remains one app.

### D-002: Diagnostic app is distinct from the final companion
**Status:** Accepted  
**Decision:** The immediate deliverable is a one-time diagnostic/reverse-engineering tool. A production, always-on replacement for HiWatchPro is a later stage.  
**Rationale:** The immediate goal is to establish the exact device protocol and feature support before building the final product.  
**Consequence:** Do not prematurely implement the permanent notification service or all companion features in the diagnostic app.

### D-003: Keep the project safe by default
**Status:** Accepted  
**Decision:** Initial discovery is read-only. No firmware, OTA/DFU, bootloader, factory reset or undocumented destructive commands. Any normal application-level write, including watch-face upload, requires protocol evidence and explicit user approval.  
**Rationale:** The user explicitly does not want to brick the watch.  
**Consequence:** Unknown commands must never be sent experimentally.

### D-004: Watch-face upload is a first-class research target
**Status:** Accepted  
**Decision:** Inspect the HiWatchPro custom watch-face workflow and reverse-engineer the input format, image processing, transfer protocol, acknowledgement and constraints.  
**Rationale:** The user knows HiWatchPro can upload a watch face from a phone image and wants to improve this capability later.  
**Consequence:** Do not assume ordinary JPEG/PNG upload or guess BLE payloads. Static APK and runtime evidence are required.

### D-005: No chipset inference from multi-platform SDKs or similar UUIDs
**Status:** Accepted  
**Decision:** Yichip/Telink/Realtek/RealSil SDK strings in the APK and the vendor service UUID are research leads, not proof of the watch chipset or protocol.  
**Rationale:** HiWatchPro may support many devices. The observed UUID differs from the common Nordic UART UUID.  
**Consequence:** Only device-specific evidence may raise a candidate to verified.

### D-006: Keep the public repository free of private/proprietary evidence
**Status:** Accepted  
**Decision:** Do not commit the original proprietary APK, unredacted device address, private notification captures, secrets or credentials.  
**Rationale:** The repository is currently public. The APK is 106.66 MiB and exceeds GitHub's normal 100 MiB single-object limit; it is also a proprietary third-party binary.  
**Consequence:** Store a fingerprint and instructions in the repo. The user can supply the APK privately to an agent. Revisit LFS/private storage only after explicit visibility and rights decision.

### D-007: One main branch
**Status:** Accepted  
**Decision:** Use main as the primary branch and avoid unnecessary long-lived branches/workflows.  
**Rationale:** The user prefers a simple repository workflow.  
**Consequence:** Make small, reversible commits and preserve user changes.

### D-008: Liquid Glass is primarily a future companion UI requirement
**Status:** Accepted  
**Decision:** Use a polished phone-friendly interface for the diagnostic tool, but do not compromise its primary research function. Full iOS 27-inspired Liquid Glass refinement belongs to the future companion if pursued.  
**Rationale:** The user wants a premium interface, but the diagnostic application is a one-time tool.

### D-009: <1% battery/24 hours is a future companion target, not a diagnostic-app guarantee
**Status:** Accepted  
**Decision:** Measure rather than promise. Keep the diagnostic app session-based and avoid unnecessary background service.  
**Rationale:** The always-on companion and a one-time diagnostic utility have different lifecycle requirements.

### D-010: Runtime HCI capture is an optional evidence route, not a third-party app requirement
**Status:** Accepted  
**Decision:** If APK static analysis and direct GATT discovery do not reveal the protocol, investigate Android's built-in Bluetooth HCI snoop logging and optionally build capture import into the diagnostic tool.  
**Rationale:** One app cannot passively inspect another app's private GATT calls through ordinary Android APIs.  
**Consequence:** Be transparent about any system-level capture/export step that cannot be fully automated from a normal app.


### D-011: Use a standalone native Compose survey, no device mutations (2026-11-14)
**Status:** Accepted for the initial 0.1.0 implementation.
**Decision:** Use Kotlin + Compose with local-only session state, strict scan/discover/un-encrypted-READ BLE operations, manual device choice, optional metadata-only H4 btsnoop file summary, local image-dimension inspection, and ZIP export. Explicit opt-in is required for raw identifiers and opaque GATT values in exports. No internet permission, background service or pairing.
**Rationale:** One deterministic inspect-and-export tool minimizes unknown effects on the watch and creates actionable device evidence. More advanced protocol work requires the proprietary APK or controlled captures and must not be guessed.
**Consequence:** This version does not decode proprietary commands, inspect another app's memory/traffic automatically, subscribe to characteristics, or upload watch faces. Actual device validation remains necessary.

### D-012: Begin separate companion after sanitized physical watch report (2026-11-14)
**Status:** Accepted. **Decision:** User explicitly advanced the project to a functional companion after successful WatchProbe inspection; defer aesthetics, preserve WatchProbe as archived tool, build separate `companion` module/package without replacing it. **Consequence:** Deliver only implemented, auditable features; never assert full app parity from a GATT inventory.

### D-013: Explicit CCCD-only passive observation is permitted (2026-11-14)
**Status:** Accepted under separate opt-in. **Decision:** A user-tapped control may subscribe to exactly the observed NOTIFY characteristics using standard BLE CCCD value 01 00, without sending guessed application payloads, changing watch settings or initiating bonding. Observe only the selected watch. **Rationale:** Enables limited safe progression on unknown protocol, with no unsolicited vendor writes. **Consequence:** Log and export watch-origin responses with privacy safeguards; review any future command separately with protocol evidence, exact bytes, reversibility and failure mode.


### D-014: Retire WatchProbe from active source and build (2026-11-15)
**Status:** Accepted; directly requested by the user. **Decision:** Delete the diagnostic-only `app/` module from main, remove its APK artifact from ongoing CI, and make `companion/` the sole built Android module. Preserve past research results in sanitized documentation and original code in Git history; do not rewrite repository history. **Rationale:** The watch survey is complete and WatchProbe is not needed to run the standalone companion. **Consequence:** User can uninstall WatchProbe from their phone after keeping the report; the companion still lacks protocol evidence for proprietary functions. A new diagnostic app should not be required unless specifically justified and approved.
