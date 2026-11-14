# Device profile — T700 Pro Max

## Identification

| Field | Current observation | Confidence |
|---|---|---|
| Marketing/model name | T700 Pro Max / T700Pro Max | Verified by user's BLE screenshots |
| Device class | Proprietary smartwatch, not Android/Wear OS | User description; not independently hardware-probed |
| Phone connection | Bluetooth LE | Verified by nRF Connect scanner screenshot |
| Advertising type | Legacy | Verified by screenshot |
| Bluetooth classic | BR/EDR Not Supported flag displayed | Verified by screenshot |
| Local name | T700Pro Max | Verified by screenshot |
| RSSI | Approximately -80 dBm in one screenshot | Single sample only |
| Manufacturer data company identifier | 0x344C shown as reserved ID | Screenshot observation; meaning unknown |
| Bonding | nRF Connect displayed NOT BONDED | Verified in screenshot at capture time |
| Chipset | Unknown | Not established |
| Firmware version | Unknown | Not read |
| Hardware revision | Unknown | Not read |
| Exact model SKU | Unknown | Marketing name may be reused across revisions |

## GATT primary services shown

The user's screenshot shows these service entries:

| Service display name | UUID | Notes |
|---|---|---|
| Generic Access | 0x1800 | Standard service |
| Generic Attribute | 0x1801 | Standard service |
| Device Information | 0x180A | Appeared twice in the screenshot; duplication not explained |
| Battery Service | 0x180F | Standard service |
| Unknown Service | 6e400001-b5a3-f393-e0a9-e50e24dcca9d | Vendor-specific UUID candidate; characteristics not yet inspected |
| Unknown Service | 0xFFFF | 16-bit vendor/unknown service |
| Human Interface Device | 0x1812 | Standard service |

No characteristic or descriptor UUIDs are recorded yet. The screenshot is a primary-service list only.

## Important UUID caution

The vendor service shown by the watch is:

6e400001-b5a3-f393-e0a9-e50e24dcca9d

Its final character is **d**. It is not identical to the commonly known Nordic UART Service UUID ending in **cca9e**. Similarity is not compatibility evidence.

Do not assume the service has RX/TX characteristics with the usual Nordic UART UUIDs. The diagnostic app must discover the actual characteristic and descriptor UUIDs and properties.

## Advertising observations

The scanner screenshot displayed:
- Complete local name: T700Pro Max.
- Device type: LE only.
- Advertising type: Legacy.
- Flags: LE Limited Discoverable; BR/EDR Not Supported.
- Manufacturer data: Company displayed as Reserved ID <0x344C>.
- The screenshot also exposed a hardware address. It is intentionally not copied into this public document.

The displayed RSSI is environmental and time-specific; it should not be used to infer range or chipset.

## What remains to be collected

- Complete raw advertising payload (sanitized).
- Complete 16-bit and 128-bit service list.
- All characteristics under each service.
- Characteristic properties: read/write/write-without-response/notify/indicate.
- All descriptors, including CCCD where present.
- Manufacturer name, model number, hardware revision, firmware revision and software revision if readable through Device Information.
- Battery characteristic value if readable.
- Connection interval/MTU/PHY where the platform provides them.
- Pairing/security requirements for each relevant operation.
- Repeatable response payloads and feature-to-command mapping.
- Firmware and hardware family indicators.

## Evidence policy

The source of current facts is the user's two nRF Connect screenshots in the originating conversation. Raw screenshots are not committed because they show a hardware address and the repository is public. The address should not be required for protocol research; the local advertised name plus UUID inventory is enough for current handoff.

Do not add a guessed chipset, board name, display controller, resolution, RAM, flash size or firmware identity without evidence.

## 2026-11-14 WatchProbe physical watch report (verified on this device)
User uploaded private sanitized `WatchProbe-20261114-2317.zip` (NOT committed or mirrored to public GitHub). Device T700Pro Max on OPPO CPH2531 Android SDK 35, GATT discovery/reads completed without writes or pairing. RSSI -70 dBm on sampled scan. Eight reported GATT service entries, 24 characteristic entries. Advertising service FFFF, manufacturer-company ID 0x344C (does not prove chipset).
- Device Information 180A: 2A29 manufacturer **LY716**, 2A26 firmware **V09708**, 2A27 hardware **01**, 2A24 model **MI RC**; 2A28 opaque bytes must not be interpreted without protocol evidence. Duplicate 180A service entries reported.
- Battery 180F characteristic 2A19: **100%** at inspection, READ+NOTIFY, CCCD 2902.
- Proprietary service **6e400001-b5a3-f393-e0a9-e50e24dcca9d**: `…0002…cca9d` WRITE and WRITE_NO_RESPONSE; `…0003…cca9d` NOTIFY with CCCD 2902. This is NOT conventional Nordic NUS UUID ending …cca9e.
- Vendor service **0000ffff-0000-1000-8000-00805f9b34fb**: `0000ff11…` NOTIFY with CCCD 2902; `0000ff22…` WRITE_NO_RESPONSE. Protocol payloads unknown. Do not send bytes to them without exact evidence and separately authorized command.
- HID 1812: encrypted/protected and/or failed standard reads; no input report semantics established, don't initiate pairing automatically.
- Protocol events file empty, no snoop capture imported, private raw evidence OFF. Thus no proprietary opcode or response payload captured. No direct evidence yet for steps/health/calls/notifications-to-watch/time sync/find-device/faces.
- External research: 2022 FocusFit Pro-Y68 reverse-engineering at https://xor.co.za/post/2022-11-30-hacking-smartwatch/ also reports a `…cca9d` service, but is NOT proof of identical firmware/protocol or chipset in T700Pro Max.


## 2026-11-15: Active companion 0.2.0 physically tested
Source: private user export `T700-Companion-20261115-0109.zip`. Watch connection=Connected, service count=8, battery=100%, manufacturer LY716, firmware V09708, hardware 01, model MI RC. App subscribed successfully to CCCDs 2A19, 6e400003-...-cca9d and FF11 (via Android GATT). Received 2 notifications of 20 bytes each on 6e400003-...-cca9d; raw evidence omitted, **no protocol opcode/meaning known**, no vendor writes sent. Do not infer watch time sync/notifications-to-watch/health/watch faces from these events. No phone-side screen or hardware manipulations were done by development agent.
