#!/usr/bin/env python3
"""Promote an already-uploaded Play version to another track without re-uploading it."""
from __future__ import annotations

import argparse
import json
import pathlib
import sys
from typing import Any

from play_publisher import (
    API_ROOT,
    INTERNAL_TRACK,
    PACKAGE_NAME,
    PRODUCTION_TRACK,
    RELEASE_STATUSES,
    ReleaseConfig,
    endpoint,
    edit_action_endpoint,
    load_service_account,
    release_payload,
    _authorized_session,
)


def promote_version(
    credentials_path: pathlib.Path,
    *,
    track: str,
    version_code: int,
    status: str,
    user_fraction: float | None,
    commit: bool,
    allow_production: bool,
) -> dict[str, Any]:
    config = ReleaseConfig(track=track, version_code=version_code)
    config.validate(allow_production=allow_production)
    payload = release_payload(
        version_code,
        track=track,
        status=status,
        user_fraction=user_fraction,
        allow_production=allow_production,
    )
    load_service_account(credentials_path)
    session = _authorized_session(credentials_path)

    response = session.post(f"{API_ROOT}/{PACKAGE_NAME}/edits", json={}, timeout=60)
    response.raise_for_status()
    edit_id = response.json()["id"]

    # A tested track is the only supported source for subsequent promotions.
    # Reading the current Play edit protects against promoting an arbitrary
    # version code that was uploaded to a different track but never reached qa.
    # A qa-to-qa edit is intentionally exempt (used to change a draft status).
    if track != INTERNAL_TRACK:
        source = session.get(
            endpoint(PACKAGE_NAME, edit_id, f"tracks/{INTERNAL_TRACK}"),
            timeout=60,
        )
        source.raise_for_status()
        source_track = source.json()
        if not isinstance(source_track, dict) or source_track.get("track") != INTERNAL_TRACK:
            raise ValueError("Unexpected Google Play Internal testing track response")
        releases = source_track.get("releases", [])
        if not isinstance(releases, list):
            raise ValueError("Malformed Google Play Internal testing releases")
        eligible = False
        for release in releases:
            if not isinstance(release, dict):
                raise ValueError("Malformed Google Play Internal testing release")
            codes = release.get("versionCodes", [])
            if not isinstance(codes, list):
                raise ValueError("Malformed Google Play Internal testing version codes")
            if str(version_code) in [str(code) for code in codes]:
                eligible = release.get("status") == "completed"
                break
        if not eligible:
            raise ValueError(
                f"Cannot promote versionCode {version_code} to {track}: "
                "the same version must already have a completed release "
                "on Google Play Internal testing (qa). "
                "Complete Internal testing and review the upgrade first."
            )

    update = session.put(
        endpoint(PACKAGE_NAME, edit_id, f"tracks/{track}"),
        json=payload,
        timeout=60,
    )
    update.raise_for_status()

    validation = session.post(edit_action_endpoint(PACKAGE_NAME, edit_id, "validate"), json={}, timeout=60)
    validation.raise_for_status()

    result: dict[str, Any] = {
        "editId": edit_id,
        "packageName": PACKAGE_NAME,
        "track": track,
        "versionCode": version_code,
        "releaseStatus": status,
        "userFraction": user_fraction,
        "committed": False,
    }
    if commit:
        committed = session.post(
            edit_action_endpoint(PACKAGE_NAME, edit_id, "commit"),
            params={"changesInReviewBehavior": "ERROR_IF_IN_REVIEW"},
            json={},
            timeout=60,
        )
        committed.raise_for_status()
        result["committed"] = True
    return result


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Guarded Google Play track promotion")
    parser.add_argument("--credentials", type=pathlib.Path, required=True)
    parser.add_argument("--track", required=True)
    parser.add_argument("--version-code", type=int, default=ReleaseConfig().version_code)
    parser.add_argument("--status", choices=tuple(sorted(RELEASE_STATUSES)), default="draft")
    parser.add_argument("--user-fraction", type=float)
    parser.add_argument("--confirm-production", action="store_true")
    parser.add_argument("--commit", action="store_true")
    args = parser.parse_args(argv)

    if args.track == PRODUCTION_TRACK and not args.confirm_production:
        parser.error("production promotion requires --confirm-production")

    result = promote_version(
        args.credentials,
        track=args.track,
        version_code=args.version_code,
        status=args.status,
        user_fraction=args.user_fraction,
        commit=args.commit,
        allow_production=args.confirm_production,
    )
    print(json.dumps(result, indent=2, sort_keys=True))
    return 0


if __name__ == "__main__":
    sys.exit(main())
