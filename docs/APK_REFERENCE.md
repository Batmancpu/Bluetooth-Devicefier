# Original companion APK — reference and fingerprint

## Source

The user attached a file named **HiWatchPro.apk** to the originating ChatGPT conversation. It is the actual companion app the user currently has for the T700 Pro Max. The user expects it to be used as a reverse-engineering reference so the eventual replacement can reproduce useful behavior while removing unnecessary tracking/advertising and improving the experience.

The exact binary is not included in this public GitHub repository. See docs/EVIDENCE/APK_STORAGE.md.

## Exact fingerprint

| Property | Value |
|---|---|
| Supplied filename | HiWatchPro.apk |
| Size in bytes | 111,842,623 |
| Size in MiB | 106.66 MiB |
| SHA-256 | d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc |
| ZIP entries | 4,623 |
| DEX files | 13 (including two under assets/audience_network) |
| Top-level lib/ entries | 0 found in archive listing |
| Version string observed in DEX/resources | 1.3.79 |
| App label string | HiWatchPro |

The package/application ID and complete Android manifest have not yet been verified with a proper binary-manifest parser in this handoff. Do not invent a package identifier from the display name.

## Static archive observations

The APK archive contains:
- AndroidManifest.xml (binary Android XML; 108,284 bytes).
- resources.arsc.
- 13 DEX files.
- Numerous assets, including localized HTML help/manual/privacy resources.
- Assets named for blood pressure, heart rate, weather and Bluetooth scanning.
- A set of HTML help/FAQ/user-guide/privacy files in multiple languages.
- No top-level native library folder entries were found in the simple ZIP listing.

String and class-name evidence includes:
- HiWatchPro and version string 1.3.79.
- xfkj.fitpro.
- com.legend.bluetooth.fitprolib.
- Yichip-related strings/classes.
- Telink OTA SDK references.
- Realtek/RealSil DFU SDK references.

These are observations of code/resources bundled in the supplied multi-device app. They do not prove that the T700 Pro Max uses Yichip, Telink or Realtek hardware. A vendor app can bundle libraries for many watch families and select one at runtime.

Previous conversation discussion mentioned that HiWatchPro supports uploading a custom watch face/photo from the phone. Treat that as an observed user-facing app capability from the conversation, but the exact file pipeline, image dimensions, compression/packing, payload framing and acknowledgement logic have not yet been reverse-engineered.

## Required analysis when exact APK is supplied to a future agent

1. Verify SHA-256 matches the fingerprint above.
2. Parse AndroidManifest.xml using a reliable Android binary XML/APK parser.
3. Record package name, versionName, versionCode, minSdk, targetSdk, permissions, services, receivers and exported components.
4. Extract app resources and localized help/manual/privacy text for analysis.
5. Decompile DEX files for static analysis; identify application-owned packages separately from third-party SDKs.
6. Search for BLE scanning, GATT connection, service UUIDs, characteristic UUIDs, command constants, packet encoders/decoders and device-family dispatch logic.
7. Map the feature flows: pairing, initial setup, time sync, notifications, phone calls, WhatsApp, alarms, health data, settings and watch-face upload.
8. Trace the watch-face pipeline: selected image → crop/resize → format conversion → metadata/template → packet fragmentation → BLE transfer → acknowledgement/result.
9. Identify any firmware-update/OTA code and explicitly exclude it from the new diagnostic/companion application.
10. Record exact class names, resource names and evidence references in docs/PROTOCOL_RESEARCH.md or a dedicated subdocument.
11. Separate vendor app's bundled code from the branch actually selected for T700 Pro Max. Only runtime/device evidence can establish that final mapping.

## Legal and handling notes

The APK is a proprietary third-party app binary supplied by the user for local analysis. Do not redistribute it publicly, upload it to a public release or commit its decompiled proprietary code wholesale. Keep analysis focused on interoperability facts, protocol behavior and independently implemented code. Respect applicable licenses and do not copy third-party SDK code into the new app.
