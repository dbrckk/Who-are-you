#!/usr/bin/env python3
"""Fail-closed Play version preflight for an in-place app update.

This creates an uncommitted Play edit, reads the app's existing APK, AAB and
track version codes, then discards that edit. It never uploads or publishes.
"""
from __future__ import annotations

import argparse
import json
import pathlib
import sys

from play_publisher import (
    API_ROOT,
    PACKAGE_NAME,
    VERSION_CODE,
    VERSION_NAME,
    _authorized_session,
    endpoint,
    load_service_account,
)


def _version_code(value: object, source: str) -> int:
    try:
        code = int(value)
    except (ValueError, TypeError) as exc:
        raise ValueError(f"Invalid Play versionCode in {source}: {value!r}") from exc
    if code < 1:
        raise ValueError(f"Invalid nonpositive Play versionCode in {source}")
    return code


def inspect_play_versions(session, edit_id: str) -> dict:
    """List previously uploaded versions across APKs, bundles and all tracks."""
    known: set[int] = set()
    tracks: list[dict] = []
    for resource, field in (("bundles", "bundles"), ("apks", "apks")):
        response = session.get(endpoint(PACKAGE_NAME, edit_id, resource), timeout=60)
        response.raise_for_status()
        payload = response.json()
        entries = payload.get(field, [])
        if not isinstance(entries, list):
            raise ValueError(f"Unexpected Play {resource} response")
        for item in entries:
            if not isinstance(item, dict) or "versionCode" not in item:
                raise ValueError(f"Play {resource} entry lacks versionCode")
            known.add(_version_code(item["versionCode"], resource))

    response = session.get(endpoint(PACKAGE_NAME, edit_id, "tracks"), timeout=60)
    response.raise_for_status()
    payload = response.json()
    entries = payload.get("tracks", [])
    if not isinstance(entries, list):
        raise ValueError("Unexpected Play tracks response")
    for track in entries:
        if not isinstance(track, dict) or not isinstance(track.get("track"), str):
            raise ValueError("Unexpected Play track entry")
        active_codes: set[int] = set()
        for release in track.get("releases", []):
            if not isinstance(release, dict) or not isinstance(release.get("versionCodes", []), list):
                raise ValueError("Unexpected Play track release")
            for code in release.get("versionCodes", []):
                active_codes.add(_version_code(code, "tracks"))
        known.update(active_codes)
        tracks.append({"track": track["track"], "versionCodes": sorted(active_codes)})
    return {"existingVersionCodes": sorted(known), "tracks": tracks}


def preflight(credentials: pathlib.Path) -> dict:
    load_service_account(credentials)
    session = _authorized_session(credentials)
    edit_response = session.post(f"{API_ROOT}/{PACKAGE_NAME}/edits", json={}, timeout=60)
    edit_response.raise_for_status()
    edit_id = edit_response.json()["id"]
    try:
        state = inspect_play_versions(session, edit_id)
    finally:
        # Do not commit a preflight edit. It expires automatically if deletion fails.
        try:
            deleted = session.delete(f"{API_ROOT}/{PACKAGE_NAME}/edits/{edit_id}", timeout=30)
            deleted.raise_for_status()
        except Exception as exc:
            print(f"Warning: temporary Play edit cleanup failed ({type(exc).__name__})", file=sys.stderr)

    previous = state["existingVersionCodes"]
    highest = max(previous, default=0)
    if VERSION_CODE <= highest:
        raise ValueError(
            f"Cannot update existing {PACKAGE_NAME}: Gradle versionCode {VERSION_CODE} "
            f"is not greater than the highest code already in Google Play ({highest}). "
            "Increase versionCode, rebuild and revalidate; do not change package/signing identity."
        )
    return {
        "packageName": PACKAGE_NAME,
        "candidateVersionName": VERSION_NAME,
        "candidateVersionCode": VERSION_CODE,
        "highestExistingVersionCode": highest,
        "existingVersionCodes": previous,
        "tracks": state["tracks"],
        "safeToAttemptUpload": True,
        "note": "Upload signing certificate and real Play-to-Play upgrade still require separate verification.",
    }


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Check the existing Play app before uploading an update.")
    parser.add_argument("--credentials", type=pathlib.Path, required=True)
    parser.add_argument("--receipt", type=pathlib.Path)
    args = parser.parse_args(argv)
    try:
        report = preflight(args.credentials)
    except (OSError, ValueError, KeyError, RuntimeError) as exc:
        print(f"Play existing-version preflight failed: {exc}", file=sys.stderr)
        return 1
    data = json.dumps(report, indent=2, sort_keys=True) + "\n"
    if args.receipt:
        args.receipt.write_text(data, encoding="utf-8")
    print(data, end="")
    return 0


if __name__ == "__main__":
    sys.exit(main())
