# HiWatchPro APK — two-part archive

## Original
- Filename: `HiWatchPro.apk`
- Size: 111,842,623 bytes (106.66 MiB)
- SHA-256: `d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc`

## Split files prepared for transfer

| Part | Exact size | SHA-256 |
|---|---:|---|
| `HiWatchPro.apk.part01` | 55,921,312 bytes | `74e033ce802fdc5d00bea29bb8f500c571b2b543db59c9389e83c28a6a634ead` |
| `HiWatchPro.apk.part02` | 55,921,311 bytes | `3b2c352331e926644f9c5243d87a7aab9b665584b581e8d6ea6039fe33c41dbb` |

The APK was split at byte offset 55,921,312. Reassembly has been tested in the originating session. The resulting file was exactly 111,842,623 bytes, had the expected SHA-256, and passed byte-for-byte comparison with the original.

The two part files are supplied as downloadable artifacts in the ChatGPT response. They are not committed to this repository.

## Reassembly

Use the repository script:

```bash
python tools/reassemble_HiWatchPro.py /path/to/parts /path/to/private-evidence/HiWatchPro.apk
```

The parts directory must contain both exact filenames. The script concatenates part01 then part02 and verifies the original file size and SHA-256. It deletes the output if verification fails.

## GitHub upload constraints

Official GitHub documentation states browser repository uploads are limited to 25 MiB per file, while command-line Git supports up to 100 MiB per file; files over 100 MiB are blocked in normal Git:
https://docs.github.com/en/repositories/working-with-files/managing-files/adding-a-file-to-a-repository

Each of these two pieces is approximately 53.33 MiB, so they are suitable for a Git command-line push by an agent, not the GitHub browser upload flow.

The repository was last observed as **public**. Do not commit the proprietary APK parts or a reconstructed APK to public Git history or public releases. Do not change repository visibility yourself. The user must change it to private and explicitly authorize storing the parts before a future agent commits them. Alternatively, keep the parts attached privately to the coding-agent session and reconstruct in its private workspace.

## Privacy

This is a proprietary third-party app binary. Do not redistribute it publicly or commit decompiled proprietary source wholesale. Preserve only technical interoperability findings in public docs.
