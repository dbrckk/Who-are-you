#!/usr/bin/env python3
import argparse
import json
from pathlib import Path
import zipfile

MIB = 1024 * 1024
DEFAULT_WARN_MIB = 30
DEFAULT_FAIL_MIB = 50


def inspect_bundle(path: Path) -> dict:
    size_bytes = path.stat().st_size
    with zipfile.ZipFile(path) as archive:
        entries = archive.infolist()
        native = [e for e in entries if "/lib/" in f"/{e.filename}" and e.filename.endswith(".so")]
        dex = [e for e in entries if e.filename.endswith(".dex")]
        resources = [e for e in entries if "/res/" in f"/{e.filename}"]
    return {
        "bundle": str(path),
        "size_bytes": size_bytes,
        "size_mib": round(size_bytes / MIB, 2),
        "entry_count": len(entries),
        "native_library_count": len(native),
        "dex_file_count": len(dex),
        "resource_entry_count": len(resources),
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Inspect and enforce the internal Play AAB size budget.")
    parser.add_argument("bundle", type=Path)
    parser.add_argument("--warn-mib", type=int, default=DEFAULT_WARN_MIB)
    parser.add_argument("--fail-mib", type=int, default=DEFAULT_FAIL_MIB)
    parser.add_argument("--json-out", type=Path)
    args = parser.parse_args()

    if not args.bundle.is_file():
        parser.error(f"Bundle not found: {args.bundle}")
    if args.warn_mib <= 0 or args.fail_mib <= 0 or args.warn_mib >= args.fail_mib:
        parser.error("Expected 0 < warn-mib < fail-mib")

    report = inspect_bundle(args.bundle)
    report["internal_warn_mib"] = args.warn_mib
    report["internal_fail_mib"] = args.fail_mib
    report["status"] = (
        "fail" if report["size_bytes"] > args.fail_mib * MIB
        else "warn" if report["size_bytes"] > args.warn_mib * MIB
        else "ok"
    )

    rendered = json.dumps(report, indent=2, sort_keys=True)
    print(rendered)
    if args.json_out:
        args.json_out.write_text(rendered + "\n", encoding="utf-8")

    return 1 if report["status"] == "fail" else 0


if __name__ == "__main__":
    raise SystemExit(main())
