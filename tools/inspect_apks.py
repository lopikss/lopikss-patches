#!/usr/bin/env python3
"""Read-only APK/XAPK inspector. No modification, network access or billing spoofing."""
from __future__ import annotations

import argparse
import contextlib
import json
import re
import shutil
import tempfile
import zipfile
from pathlib import Path
from typing import Any, Iterator

DEX_PATTERN = re.compile(r"^classes(?:\d+)?\.dex$")
MARKERS = {
    "billing_8_3_0": b"billing@@8.3.0",
    "proxy_billing_activity": b"Lcom/android/billingclient/api/ProxyBillingActivity;",
    "billing_activity_result": b"Activity finished with resultCode ",
    "billing_intent_source": b"INTENT_SOURCE",
    "billing_buy_intent": b"BUY_INTENT",
}


@contextlib.contextmanager
def apk_members(path: Path) -> Iterator[list[zipfile.ZipFile]]:
    """Open a plain APK or the nested APK splits inside XAPK/APKM/APKS."""
    with contextlib.ExitStack() as stack:
        root = stack.enter_context(zipfile.ZipFile(path))
        if path.suffix.lower() not in (".xapk", ".apkm", ".apks"):
            yield [root]
            return

        archives: list[zipfile.ZipFile] = []
        children = sorted(
            (name for name in root.namelist() if name.lower().endswith(".apk")),
            key=lambda name: ("base" not in name.lower() and "weaponrpg" not in name.lower(), name),
        )
        if not children:
            raise ValueError(f"No embedded APKs in {path}")
        for name in children:
            tmp = stack.enter_context(tempfile.TemporaryFile())
            with root.open(name) as entry:
                shutil.copyfileobj(entry, tmp, length=2 * 1024 * 1024)
            tmp.seek(0)
            archives.append(stack.enter_context(zipfile.ZipFile(tmp)))
        yield archives


def scan(path: Path) -> dict[str, Any]:
    found = {key: False for key in MARKERS}
    files: dict[str, dict[str, int]] = {}
    dex_list: list[str] = []
    with apk_members(path) as packages:
        for apk in packages:
            for member in apk.infolist():
                if member.is_dir() or member.filename == "AndroidManifest.xml":
                    continue
                if member.filename not in files:
                    files[member.filename] = {"size": member.file_size, "crc32": member.CRC}
                if DEX_PATTERN.fullmatch(member.filename):
                    dex_list.append(member.filename)
                    payload = apk.read(member)
                    for marker, token in MARKERS.items():
                        if token in payload:
                            found[marker] = True
    return {
        "name": path.name,
        "file_size_mb": round(path.stat().st_size / 1_000_000, 1),
        "dex_files": sorted(set(dex_list)),
        "markers": found,
        "files": files,
    }


def compare(original: dict[str, Any], patched: dict[str, Any]) -> dict[str, Any]:
    before, after = original["files"], patched["files"]
    shared = set(before) & set(after)
    changed = sorted(name for name in shared if before[name] != after[name])
    unity_paths = [
        name for name in shared
        if name.endswith(("libil2cpp.so", "global-metadata.dat", "libunity.so"))
    ]
    return {
        "added_count": len(set(after) - set(before)),
        "removed_count": len(set(before) - set(after)),
        "changed_dex": [name for name in changed if DEX_PATTERN.fullmatch(name)],
        "changed_unity_files": [name for name in changed if name in unity_paths],
        "unchanged_unity_files": [name for name in unity_paths if name not in changed],
        "note": "CRC comparison detects changed contents, not why a shop fails.",
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("original", type=Path, help="Original APK/XAPK/APKM")
    parser.add_argument("--patched", type=Path, help="Optional modified APK/XAPK")
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args()

    original = scan(args.original)
    patched = scan(args.patched) if args.patched else None
    report: dict[str, Any] = {
        "original": {key: value for key, value in original.items() if key != "files"},
    }
    if patched:
        report["patched"] = {key: value for key, value in patched.items() if key != "files"}
        report["comparison"] = compare(original, patched)
    if args.json:
        print(json.dumps(report, indent=2, sort_keys=True))
    else:
        print(f"Original: {original['name']} ({original['file_size_mb']} MB)")
        for name, flag in original["markers"].items():
            print(f"  {name}: {'FOUND' if flag else 'not found'}")
        if patched:
            print(f"Patched: {patched['name']} ({patched['file_size_mb']} MB)")
            result = report["comparison"]
            print("Changed DEX files:", ", ".join(result["changed_dex"]) or "none")
            print("Changed Unity files:", ", ".join(result["changed_unity_files"]) or "none")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
