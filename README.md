# T700 Companion — Bluetooth-Devicefier

**Active deliverable:** A privacy-first Android companion for the user's **T700 Pro Max** smartwatch. This repository builds **one Android APK**, the `companion/` module (`io.github.batmancpu.watchcompanion`). Functional BLE work comes first; visual refinement comes later.

## Current state — 2026-11-15

- **T700 Companion 0.3.0** is implemented, compiled, unit-tested and signature-verified in [GitHub Actions run 43623972393](https://github.com/Batmancpu/Bluetooth-Devicefier/actions/runs/43623972393). It supports T700-only scanning/manual connection, standard battery and device-info reads, and explicit opt-in temporary notifications on watch-observed CCCD paths, with private-by-default ZIP export. Its behavior on this particular watch is not yet independently verified.
- **WatchProbe is retired.** It already gathered a successful watch diagnostic ZIP, which confirmed 8 GATT service entries, 24 characteristics, manufacturer string LY716, firmware V09708 and observed vendor endpoints. At the user's request, its `app/` module and GitHub Actions APK artifact were removed from the active repository on 2026-11-15. Its source remains in Git history. The user may uninstall WatchProbe from their phone; the sanitized findings remain in [device profile](docs/DEVICE_PROFILE.md).
- **Unsupported until validated:** notification forwarding, fitness and health sync, alarms, watch faces, proprietary control commands. The protocol cannot be safely guessed from GATT UUIDs alone. The original proprietary HiWatchPro APK binary is not present in the active repository or agent environment.
- **Safety:** No firmware flashing, OTA/DFU, reset, guessed proprietary write packets, automatic bonding, background tracking or network permission.

## Feature lab v0.3.0 (2026-11-15)

The next functional APK adds opt-in phone-side **WhatsApp/SMS/phone call notification metadata detection** (no text/sender/number captured), a guided watch/phone event correlation test lab, an offline phone-image to PNG face preview generator, a visible phone clock check, user-prompted Bluetooth enable, scan debouncing, and structured local ZIP export. No unsupported watch-changing commands were added. The actual watch-specific time sync, message/call forwarding, health syncing and face upload remain blocked by original companion APK byte-level analysis or authenticated watch-specific packet traces. See [complete feature plan](docs/FEATURE_ROADMAP.md).

**Latest physical evidence:** User's private v0.2.0 ZIP proves connected watch, battery/identity reads, all three CCCD subscriptions, and **two 20-byte watch-origin vendor notifications**. Raw content was not exported; no opcode/semantics were decoded. Version 0.3.0 requires real-phone testing after the CI build.

## Build only the companion

```bash
gradle :companion:testDebugUnitTest :companion:assembleDebug
```

GitHub Actions builds, validates with Android `apksigner`/`zipalign`/`aapt`, and uploads the artifact **T700-Companion-0.3.0-debug-APK**. Download the artifact ZIP and install its `companion-debug.apk` file. Debug APK signatures may vary across CI runs, so upgrades can require uninstalling the old debug APK; stable release signing is a separate task.

## Handoff and evidence

Read [AGENTS.md](AGENTS.md) → [NEXT_AGENT_PROMPT.md](NEXT_AGENT_PROMPT.md) → [docs/PROJECT_STATE.md](docs/PROJECT_STATE.md) → [docs/COMPANION_STAGE1.md](docs/COMPANION_STAGE1.md). See [docs/DEVICE_PROFILE.md](docs/DEVICE_PROFILE.md) for physical watch evidence and [docs/DECISIONS.md](docs/DECISIONS.md) for accepted safety decisions.

The original WatchProbe findings are historical research, not a dependency for the companion app at runtime. Private watch captures and HiWatchPro proprietary binaries must not be committed to this public repository.
