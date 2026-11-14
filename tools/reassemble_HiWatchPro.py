#!/usr/bin/env python3
"""Reassemble privately supplied HiWatchPro APK parts and verify the exact original fingerprint."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import sys

EXPECTED_SIZE = 111_842_623
EXPECTED_SHA256 = "d3d724530619627c563ec94998c0fcf0e9c15eedcf457603a2fbc67c2cf598fc"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as f:
        for block in iter(lambda: f.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("parts_dir", type=Path, help="Directory containing both part files")
    parser.add_argument("output", type=Path, help="Destination APK path")
    args = parser.parse_args()
    parts = [args.parts_dir / "HiWatchPro.apk.part01",
             args.parts_dir / "HiWatchPro.apk.part02"]
    for part in parts:
        if not part.is_file():
            print(f"ERROR: missing part: {part}", file=sys.stderr)
            return 2
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("wb") as output:
        for part in parts:
            with part.open("rb") as source:
                for block in iter(lambda: source.read(1024 * 1024), b""):
                    output.write(block)
    size = args.output.stat().st_size
    digest = sha256(args.output)
    print(f"Output: {args.output}\nSize: {size} bytes\nSHA-256: {digest}")
    if size != EXPECTED_SIZE or digest != EXPECTED_SHA256:
        args.output.unlink(missing_ok=True)
        print("ERROR: fingerprint mismatch; reconstructed output deleted.", file=sys.stderr)
        return 3
    print("VERIFIED: exact match to the supplied HiWatchPro.apk.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
