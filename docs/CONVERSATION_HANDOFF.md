# Conversation handoff — user intent and history

This file is a durable reconstruction of the project conversation available when the repository was initialized. It preserves the user's prompts in order and summarizes assistant responses/decisions. It is not a platform-exported raw transcript; screenshots and binary attachments are described, not embedded. For the current exact state, also read docs/PROJECT_STATE.md.

## Conversation participants and context

The user bought a Chinese smartwatch marketed as T700 Pro Max for approximately ₹1,200 in India. It is not an Android watch. Its companion app is HiWatchPro. The user wants to understand the actual watch, remove dependence on the vendor app if possible, remove unnecessary telemetry/advertising, improve watch faces, and possibly build a better open-source companion. They have no USB/data cable for the watch and want Bluetooth-only interaction. Their overriding hardware constraint is: do not brick the watch.

The user is comfortable using GitHub and AI coding agents. They want project memory stored inside the repository so a new AI agent can continue when a conversation context limit is reached. Each agent must record every meaningful change, goal/plan change, findings, blockers and next steps.

## User prompts, chronological

### 1. Initial project idea
> Hey ChatGPT, this is an application of mine of an watch that I bought. It is a Chinese watch. And I was thinking, I don't use this app, but I was thinking my watch is not Android. It has some features like message syncing and all, but I was thinking maybe I could make an open source version of this application, regenerate, and generate back, remove all the telemetry, all the useless things, make it very good and maybe get even better watch faces and maybe, you know, update my whole watch system. But I don't have a USB or anything to connect directly to my watch. The only thing I can do with my watch is using Bluetooth. So I wanted to know, should I do it? And can this be done? If you want to know the exact watch, it is T700 Pro Max. You can search on Amazon. I bought it around 1.2K. Please search it up properly and find out the best thing. And what I was thinking, can it be done or not? It can be done. But can you do it or not? If you want to ask more questions, ask, search deeply, search on Reddit and other multiple trusted sources and answer me. And I don't want to brick my watch at all, dude. Answer me in brief, but search deeply

**Assistant conclusion:** A replacement phone companion may be feasible over BLE, but the watch is not Android/Wear OS and firmware replacement is a separate, high-risk task. The user's no-brick requirement means no firmware flashing/bootloader experiments. Exact chipset and T700 variant were not yet established.

### 2. Link request
> Give me the link for this NRF Connect.

**Assistant response:** Linked Nordic Semiconductor's nRF Connect for Mobile Android app on Google Play. The user installed/opened it and performed a scan.

### 3. BLE screenshots
The user supplied two nRF Connect screenshots rather than a written prompt.

**Screenshot findings:** T700Pro Max was visible as an LE-only, legacy-advertising device. The scanner displayed flags LE Limited Discoverable and BR/EDR Not Supported and a manufacturer data company identifier shown as reserved ID 0x344C. GATT primary services shown included 0x1800, 0x1801, 0x180A, 0x180F, 0x1812, 0xFFFF and 6e400001-b5a3-f393-e0a9-e50e24dcca9d. The hardware address is intentionally redacted in this public repo.

**Assistant conclusion:** The vendor service looked interesting, but a UUID alone does not reveal the protocol. A prior assistant response speculated about StarMax/Beken and Nordic UART-like characteristics. Treat those as unverified leads; do not use them as facts. The service UUID ending is cca9d, not cca9e.

### 4. User requests a custom diagnostic app
> Hey, can I not ask you to make an, like I'll make a GitHub empty GitHub repository and you would make an app for me for this purpose. You would do that and you could easily do that, right? And you will create an app that will connect to this application through Bluetooth from my phone and it will get all the important required information whatever is needed without harming anything, without writing anything. And then it will be a one-shot app creation for the purpose. You already have an app that we need to reverse engineer and we'll have an app to know about the device that I have. It will all be done, dude

**Assistant conclusion:** Build a native Android diagnostic app through GitHub, initially safe/read-only: scan, connect, enumerate GATT, read permitted attributes and export JSON. Use the original HiWatchPro APK as the static reverse-engineering reference. No writes or firmware changes in the initial discovery pass.

### 5. User asks for a master prompt and complete agent memory
> So also tell me one more thing and also after telling me this thing, I want you to do one thing and that is to, you know, create an extremely detailed prompt that I'll give to my AI to do this after getting a repository. So create an extremely detailed prompt. Also include in every prompt to create an agent.md file. That will, in the GitHub repository, give all the context, all the chat, all the developments, goal and everything. Also upload the whole APK into the repository somewhere so that the AI agent has full fucking context. Okay, let's go..

**Assistant response:** Recommended separation between foreground Liquid Glass UI and minimal background companion engine; Kotlin/Jetpack Compose; ambitious <1% battery/24-hour target was discussed for the future always-on companion, not guaranteed. A detailed implementation prompt was drafted, including AGENTS.md and supporting project docs.

### 6. Existing app or new app?
> So tell me, do we need to make a new app for this or is there already an app available? I am right now creating a repository. If we need, then I'll just make it.

**Assistant response:** Gadgetbridge was mentioned as an existing open-source companion, but compatibility with this exact T700 Pro Max was not confirmed. No compatibility should be assumed.

### 7. User clarifies that they do not want a chain of existing apps
> Dude, we are not stupid. I'm not going to install an application. Why don't you search what information is needed for the app development and the work that we are going to do? And why don't then you search if there is any app available that does all of that? And if there is available, then okay, otherwise we'll create an app. How much the app will be? Probably a one shot. It will not be an issue. It is not the final app, it is the app to get the information about the Bluetooth device

**Assistant response:** Discussed BLE tools such as BleSmith, nRF Connect, LightBlue and PerliBlue. The user had already used nRF Connect and did not want to install a chain of extra test utilities. The project direction returned to one purpose-built diagnostic tool. Treat previous claims about third-party tools, feature coverage and privacy declarations as research leads, not decisions or proven suitability.

### 8. User clarifies the exact purpose and watch-face requirement
> The objective is to, you know, we have an app that works, and the objective is to find enough information about the device, the Bluetooth watch, everything about that. And we have an app, so we would not need anything else, any other testing and all. We can one-shot make an app that does what the app already does, something more, better, and that can be done, and we'll be all done with it, right? And this app also permitted us to upload watch faces, so maybe we can do something nice with that as well.

**Assistant conclusion:** Aim for one comprehensive diagnostic/research application that combines device discovery, GATT inspection, safe reads, APK static analysis and watch-face workflow investigation. The vendor app can be used as a reference, but a normal Android app cannot magically inspect another app's private GATT calls. Static APK analysis may need to be complemented by a controlled Bluetooth HCI capture if exact runtime traffic is necessary. Watch-face upload is a research priority; it is not firmware flashing.

### 9. User creates the repository and requests full project handoff
> Now I have created a device identifier repository in my GitHub. You have full access to it. I want you to upload our whole chat, our aim, our context for any AI agent that is going to inherit that repository because my chat limit might get over. And also ask any AI agent who is using them to store every changes, goal, intended goal changes in goal, plan, and every possible thing so that it feels like even if I change AI agents and use that repository, I just need to connect the repository and the AI agent will pick up from like the exact end of the chat where I left with last AI agent. Are you getting me? Also upload the whole APK into the repository somewhere so that the AI agent has full fucking context. Okay, let's go..

**Assistant actions/conclusions:** Located repository Batmancpu/Bluetooth-Devicefier, default branch main, initially public and with only a minimal README. Fingerprinted the APK: 111,842,623 bytes (106.66 MiB), SHA-256 d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc. GitHub blocks normal Git objects above 100 MiB. Because the repository is public and the APK is proprietary, it was not uploaded. Its fingerprint and private handling instructions are recorded instead.

## Stable user requirements distilled from the conversation

1. Never risk bricking the T700 Pro Max.
2. Bluetooth is the only available practical device connection; no USB/data cable.
3. Use the supplied HiWatchPro APK as a technical reference, not as a redistributable asset.
4. Build one comprehensive diagnostic/research app rather than sending the user through a sequence of unrelated apps.
5. Collect as much verified information as possible in one guided workflow.
6. Treat watch-face upload as a first-class protocol and format research target.
7. Keep a clean, open-source direction for the later replacement companion.
8. Remove advertisements, analytics, telemetry and unnecessary permissions from the eventual replacement.
9. Preserve useful existing functions if technically supported, including notifications, WhatsApp, calls, health and settings.
10. The future companion should have an iOS 27-inspired Liquid Glass UI and an extremely lightweight background engine.
11. The user's <1% battery use per 24 hours is an aspiration to measure for the future always-on companion, not a promise and not a requirement for the one-shot diagnostic app.
12. Every AI agent must read the repository's handoff and update project state, session log, decisions, plans and evidence before finishing a work session.
13. Use one main branch and avoid unnecessary project infrastructure.

## Important technical correction to carry forward

Do not claim that a one-shot diagnostic app can obtain literally every hidden device fact or automatically observe another Android app's private BLE operations. It can enumerate exposed BLE data, read supported attributes, analyze local assets and import supported capture files. Runtime traffic from HiWatchPro may require Android's HCI snoop capture or another explicit observation route. The goal is maximum evidence with clear unknowns, not false certainty.

## 2026-11-14 Stage-two update
After WatchProbe installed successfully, user exported sanitized ZIP. Physical watch verified battery 100%, LY716 manufacturer string, V09708 firmware, exact two vendor notify/write channel pairs. User authorized development of permanent functional app and deferral of aesthetics. New `companion` module version 0.2.0 uses separate app identity, safe standard GATT reads and optional explicit CCCD notification monitor; watch commands/advanced features not yet enabled. Full details docs/COMPANION_STAGE1.md. Original HiWatchPro APK binary was referenced from older session by hash but is not accessible to current agent. Build/test and physical validation awaited.


## 2026-11-15: WatchProbe retirement request
User said: "If watch probe has no issue, no use, then just burn it. Why do I even need it?" The one-time diagnostic export succeeded and physical watch data is documented. Repository cleaned to companion-only active module and CI. User can uninstall WatchProbe locally. No other personal data or the original HiWatchPro APK was removed from the repository (not present).


## 2026-11-15: Complete-feature implementation request
User rejects tiny updates and explicitly requests a proper plan and substantial functional features: clock sync, watch faces, messages, phone calls, telemetry removal, health and all other companion capabilities. Provided latest T700 Companion ZIP; verified connected, battery and identity, CCCDs and two 20-byte proprietary watch-origin packets. HiWatchPro APK binary inaccessible in current session; its historical fingerprint retained. Respond with evidenced stages, implement safe phone-side event detection, offline face prep and guided watch packet correlation now, do NOT claim outbound watch features implemented until opcode proven. v0.3.0 source created, build/test to be verified. See FEATURE_ROADMAP.md.
