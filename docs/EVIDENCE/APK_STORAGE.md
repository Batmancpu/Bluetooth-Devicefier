# Original APK storage and access plan

## Current situation

The user provided HiWatchPro.apk for this project. It is available in the originating ChatGPT conversation attachment, not as a file in this GitHub repository.

Verified file:
- Size: 111,842,623 bytes (106.66 MiB).
- SHA-256: d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc.

The GitHub repository Batmancpu/Bluetooth-Devicefier was observed as **public** when this document was written.

## Why it was not uploaded

1. GitHub's normal Git object limit is 100 MiB; the supplied APK is 106.66 MiB. GitHub requires Git LFS for files larger than 100 MiB. Official documentation: https://docs.github.com/en/repositories/working-with-files/managing-large-files/about-large-files-on-github and https://docs.github.com/en/repositories/working-with-files/managing-files/adding-a-file-to-a-repository
2. The APK is a proprietary third-party application. Putting it in a public repository or public release would distribute the vendor's binary to anyone. The user asked for it as context for analysis, not explicitly for public redistribution rights.
3. The available GitHub connector in this session can commit text files and Git objects but does not expose a supported Git LFS upload or release-asset upload operation.

Do not split/base64-encode the APK into text files to bypass GitHub's size or distribution controls.

## How a future agent can access it

Preferred immediate route:
- The user attaches the original HiWatchPro.apk privately to the new AI agent's chat/workspace.
- The agent verifies the SHA-256 exactly matches the value above before analysis.
- The agent records that the file was available only in the agent session and does not add it to public Git history.
- If the hash differs, stop and ask whether this is a newer/different app build.

Alternative only after user approval:
- The user changes repository visibility to private and chooses an appropriate private large-file storage route (for example Git LFS or a private release/artifact location), after checking applicable storage/bandwidth quotas and terms.
- Do not change repository visibility yourself or assume the user wants it private.
- Do not upload a proprietary binary to a public location without clear rights and approval.

## Do not confuse these items

- APK fingerprint and static analysis notes are safe project metadata; they are stored in docs/APK_REFERENCE.md.
- The binary itself is not stored in GitHub.
- The app's extracted proprietary code is not stored here.
- A future agent must not claim to have inspected the APK unless it has the exact binary or an authenticated analysis artifact.
