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
