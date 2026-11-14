# Research objective and diagnostic-app requirements

## 1. Product objective

Build one comprehensive Android diagnostic/research application (working name: WatchProbe) for the user's T700 Pro Max Bluetooth smartwatch.

This is a one-time evidence collection tool. It is not the final always-on companion application.

The user wants enough verified knowledge of the specific physical watch and the working HiWatchPro app to make an informed implementation of a superior open-source replacement later. Do not turn this into a sequence of unrelated tool installations. nRF Connect has already been used; its screenshot evidence is documented in docs/EVIDENCE/NRF_CONNECT_OBSERVATIONS.md.

## 2. Required information set

### A. Advertising and connection
- Complete local name.
- Advertising type and flags.
- Raw advertising and scan-response data, sanitized.
- Manufacturer-specific data and service data.
- RSSI samples over a short user-initiated observation window.
- Address type and privacy behavior where available.
- Connect/disconnect behavior and error details.
- Pairing/bonding status; do not trigger pairing automatically.
- MTU and connection parameters where exposed by Android.

### B. GATT database
For every service:
- Full UUID and primary/secondary status.
- Recognized Bluetooth SIG name, where applicable.
- All characteristics.
- Full UUID and characteristic properties.
- READ / WRITE / WRITE WITHOUT RESPONSE / NOTIFY / INDICATE flags.
- Permissions and security requirements if accessible.
- All descriptors and descriptor UUIDs.
- Safe read result, raw bytes and decoded representation, for characteristics that explicitly expose READ.
- Read error, timeout and unsupported-operation records.

The application must never issue writes merely to discover whether writing works.

### C. Device information
Read, where supported and permitted:
- Manufacturer Name String.
- Model Number String.
- Serial Number String (treat as sensitive and mask by default).
- Hardware Revision String.
- Firmware Revision String.
- Software Revision String.
- System ID / PnP ID where available.
- Battery level.
- Any documented vendor characteristic whose read semantics are verified.

### D. Capability and protocol evidence
- Enumerate NOTIFY and INDICATE characteristics, but do not enable them automatically in the strict read-only discovery pass because enabling them generally requires writing a CCCD descriptor.
- Design a separate, explicit, user-approved protocol-observation phase after a characteristic is identified and the operation's effect is understood.
- Store raw packet bytes, timestamp, direction, service/characteristic UUID, MTU/chunk boundaries and decoded hypotheses.
- Maintain a confidence/evidence label for every protocol claim.
- Correlate APK static code evidence with physical-device evidence.

### E. HiWatchPro feature map
Inspect the supplied APK for the logic corresponding to:
- Device discovery, connection and reconnection.
- Initial setup and device-family selection.
- Time/date synchronization.
- Notification/app message sync.
- Incoming call and calling features.
- WhatsApp notification behavior.
- Alarm and reminder features.
- Activity, steps, sleep, heart rate, SpO2 and other health-data flows if present.
- User settings and watch settings.
- Watch-face gallery and custom photo/image watch-face upload.
- Data sync and history retrieval.
- Firmware update/OTA code (research only; never implement or invoke).

Distinguish "present in APK" from "used by this exact T700 watch".

### F. Watch-face research
The user specifically noted that HiWatchPro permits uploading a watch face using a phone image/photo workflow.

Investigate:
- Whether the input is a static photo, template or structured watch-face package.
- Required aspect ratio and pixel dimensions.
- Cropping and scaling behavior.
- JPEG/PNG/BMP or proprietary source format.
- Color depth, compression, palette, dithering and byte order.
- Watch-face layout/template metadata.
- Placement of time/date/battery overlays.
- File size and image size limits.
- Whether one or multiple watch-face slots exist.
- BLE transfer initiation, command framing, chunk size, sequence IDs, acknowledgements, retries and completion status.
- Any watch-side validation or rollback behavior.

Never attempt a transfer with a guessed format or packet sequence. A test transfer, when eventually proposed, must be a separately reviewed normal application-level operation with a known-good asset, clear expected result and user approval. Never use OTA/DFU/firmware code for watch-face work.

### G. Runtime traffic
Static APK inspection is essential but may not reveal which exact branch or command sequence the physical watch uses. A regular Android application cannot directly read another app's private GATT operations.

If runtime traffic is needed:
- Investigate Android Bluetooth HCI snoop logging on the user's OPPO/ColorOS build.
- Prefer capturing only controlled actions, with dummy notifications and a test watch face.
- The diagnostic app may include an import/parser for a user-supplied Bluetooth snoop log if feasible.
- Explain any required Developer Options interaction; do not silently require a PC/ADB.
- Redact identifiers and personal payloads before export.
- Do not claim a capture is complete if Android did not expose the relevant traffic.

## 3. One-app design

The diagnostic tool should combine:
1. BLE scanner and advertising inspector.
2. GATT browser and safe reader.
3. Device-information dashboard.
4. Protocol evidence recorder.
5. Optional HCI snoop file importer/analyzer.
6. APK-derived feature/protocol notes (the APK itself can be analyzed by the coding agent; do not embed proprietary APK content into the app).
7. Local watch-face asset inspector for candidate images.
8. Research bundle exporter: JSON, Markdown/HTML report, raw sanitized captures and image metadata.

The goal is to minimize repeated manual steps. However, no app can discover hidden data or all command meanings automatically. Unknowns must remain explicit.

## 4. Safety and permission model

- Strict read-only discovery by default.
- Do not pair automatically.
- Do not write characteristics or descriptors in the discovery pass.
- No OTA, DFU, firmware update, bootloader, reset or destructive commands.
- Any non-read BLE operation requires documented semantics, a risk note and explicit user confirmation.
- Notification/indication subscriptions change CCCD state and therefore do not belong in the initial strict read-only pass.
- Ask only for Android permissions required by the selected APIs and OS version.
- No advertising/analytics SDKs.
- No account, backend or cloud upload.
- No internet permission unless an explicit local-only design requirement proves it necessary.
- Export on-device; user chooses what to share.
- Mask Bluetooth addresses and serial identifiers by default.
- Do not log personal message/call content.
- Clear sensitive temporary buffers where practical.

## 5. UI and performance

The diagnostic tool should be polished and easy to operate on a phone. The user's preferred visual direction is iOS 27-inspired Liquid Glass: refined translucent surfaces, subtle depth, restrained motion, elegant typography and excellent dark mode.

Do not overbuild the diagnostic tool's background behavior. It is a one-time research utility, not a persistent service.

The user's target of less than 1% battery consumption over 24 hours applies to the future always-on companion. It is an ambitious measured goal, not a guarantee. For that future app, keep foreground UI separate from the minimal event-driven connection/notification engine.

## 6. Export package

One export should include:
- report.md or report.html;
- device.json;
- advertising.json;
- gatt.json;
- readable-values.json;
- protocol-events.jsonl;
- optional capture reference and parser summary;
- watch-face input-image metadata;
- apk-reference.json containing file name, SHA-256 and static analysis notes (not APK bytes);
- environment.json;
- diagnostics for errors/timeouts;
- README explaining evidence labels and privacy redactions.

Never include personal notifications, contacts, message bodies, phone numbers, unredacted serials/MAC addresses, the proprietary APK or secrets by default.

## 7. Completion criteria

The diagnostic app is useful when it can:
- Scan and identify the T700 Pro Max.
- Connect safely.
- Fully enumerate available GATT structure.
- Read allowed standard/device information.
- Export a complete structured report.
- Preserve evidence and confidence labels.
- Import/analyze a supported Bluetooth capture if implemented.
- Inspect candidate watch-face image metadata.
- Explain what remains unknown and why.

The app is not "complete" merely because it connects. Protocol, feature and watch-face claims require evidence. The future companion can only be implemented accurately after gaps are documented.
