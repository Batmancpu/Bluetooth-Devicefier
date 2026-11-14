# nRF Connect screenshot observations

**Source:** two screenshots supplied by the user in the originating conversation on 2026-11-14. These are transcribed manually. The original screenshots are not committed because the repository is public and the image visibly contains a Bluetooth hardware address.

## Scanner screenshot

- Device shown: T700Pro Max.
- Device type: LE only.
- Advertising type: Legacy.
- Flags displayed: LE Limited Discoverable; BR/EDR Not Supported.
- RSSI at that moment: -80 dBm.
- Complete Local Name: T700Pro Max.
- Manufacturer data displayed as Bluetooth Core 4.1 manufacturer data.
- Company displayed: Reserved ID <0x344C>.
- Raw manufacturer bytes included data resembling the watch's hardware address; those bytes are not copied here.

## GATT services screenshot

nRF Connect showed the device as connected (top action showed DISCONNECT) and NOT BONDED.

Primary services visible:
- Generic Access — 0x1800.
- Generic Attribute — 0x1801.
- Device Information — 0x180A.
- Battery Service — 0x180F.
- Unknown/vendor — 6e400001-b5a3-f393-e0a9-e50e24dcca9d.
- Unknown — 0xFFFF.
- Device Information — 0x180A (second visual entry).
- Human Interface Device — 0x1812.

The vendor UUID wraps across two lines in the screenshot. Its full text is recorded above.

## What these screenshots do not show

- No expanded characteristic list.
- No characteristic UUIDs/properties.
- No descriptors.
- No values read from Device Information.
- No Battery Level value.
- No chipset, firmware revision or hardware revision.
- No application-level command or data exchange.

## Follow-up collection goals

A purpose-built diagnostic app should repeat discovery and export all service/characteristic/descriptor data in a machine-readable report. It should perform only reads on characteristics that expose the READ property during its safe discovery pass.
