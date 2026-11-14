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
