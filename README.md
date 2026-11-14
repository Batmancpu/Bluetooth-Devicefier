# Bluetooth-Devicefier

**Purpose:** identify and reverse-engineer the Bluetooth behavior of the user's T700 Pro Max smartwatch so a technically accurate, privacy-first replacement companion can eventually be built.

The current deliverable is a **comprehensive, one-time Android diagnostic/research application**. It is not yet the final always-on smartwatch companion. The research app should collect the maximum useful device, BLE, protocol and watch-face evidence in one controlled workflow, then export a handoff package for protocol analysis.

## Start here (mandatory for every AI agent)

1. Read [AGENTS.md](AGENTS.md) before doing any work.
2. Read [docs/PROJECT_STATE.md](docs/PROJECT_STATE.md) for the latest state and next action.
3. Read [docs/DEVICE_PROFILE.md](docs/DEVICE_PROFILE.md) and [docs/APK_REFERENCE.md](docs/APK_REFERENCE.md).
4. Read [docs/CONVERSATION_HANDOFF.md](docs/CONVERSATION_HANDOFF.md) for user intent, decisions and conversation history.
5. Read the latest entry in [docs/SESSION_LOG.md](docs/SESSION_LOG.md).

## Current status

- Repository documentation and handoff are being established.
- No Android application source has been implemented yet.
- The user's original HiWatchPro APK was supplied in the conversation, inspected at a basic archive/string level, and fingerprinted. The APK itself is **not committed**: the repository is public, the file is 106.66 MiB (above GitHub's 100 MiB regular Git object limit), and it is a proprietary app binary. See [docs/APK_REFERENCE.md](docs/APK_REFERENCE.md).
- The user has already used nRF Connect for Mobile and supplied screenshots showing the watch's BLE advertising record and primary GATT services.

## Non-negotiable constraints

- Do not brick, reset, reflash, unlock, or otherwise risk the watch.
- No firmware, bootloader, DFU, OTA, factory-reset or undocumented destructive commands.
- Do not invent chipset identity or protocol compatibility from a UUID or a library bundled in the APK.
- No USB cable or desktop workflow may be assumed for the user's device-side steps; the user has an OPPO Reno10 5G running Android 15 / ColorOS 15 and can work from Android plus GitHub.
- Preserve one primary branch, **main**. Do not create unnecessary long-lived branches.
- Keep the repository's documentation current so another AI can continue without the previous chat.
- Do not commit the proprietary APK, Bluetooth MAC address, captured personal notifications, snoop logs containing private data, credentials or signing keys to this public repository.

## Documentation map

- [AGENTS.md](AGENTS.md): mandatory agent operating rules.
- [agent.md](agent.md): case-compatible pointer to AGENTS.md.
- [docs/PROJECT_STATE.md](docs/PROJECT_STATE.md): current objective, status, blockers and next action.
- [docs/DEVICE_PROFILE.md](docs/DEVICE_PROFILE.md): verified watch and BLE observations.
- [docs/APK_REFERENCE.md](docs/APK_REFERENCE.md): supplied APK fingerprint and verified archive findings.
- [docs/RESEARCH_AND_REQUIREMENTS.md](docs/RESEARCH_AND_REQUIREMENTS.md): diagnostic-app requirements and research plan.
- [docs/DECISIONS.md](docs/DECISIONS.md): accepted decisions and rejected assumptions.
- [docs/CONVERSATION_HANDOFF.md](docs/CONVERSATION_HANDOFF.md): user prompts reproduced where available and assistant conclusions summarized.
- [docs/SESSION_LOG.md](docs/SESSION_LOG.md): chronological work log.
- [docs/EVIDENCE/NRF_CONNECT_OBSERVATIONS.md](docs/EVIDENCE/NRF_CONNECT_OBSERVATIONS.md): screenshot transcription with private address redacted.
- [docs/EVIDENCE/APK_STORAGE.md](docs/EVIDENCE/APK_STORAGE.md): why the APK is not in this public repository and how to supply it safely.

The repo name is **Bluetooth-Devicefier**. The proposed diagnostic app name **WatchProbe** is provisional and can be changed by the user.
