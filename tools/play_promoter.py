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
    PACKAGE_NAME,
    PRODUCTION_TRACK,
    RELEASE_STATUSES,
    ReleaseConfig,
    endpoint,
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

    update = session.put(
        endpoint(PACKAGE_NAME, edit_id, f"tracks/{track}"),
        json=payload,
        timeout=60,
    )
    update.raise_for_status()

    validation = session.post(endpoint(PACKAGE_NAME, edit_id, "validate"), json={}, timeout=60)
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
        committed = session.post(endpoint(PACKAGE_NAME, edit_id, "commit"), json={}, timeout=60)
        committed.raise_for_status()
        result["committed"] = True
    return result


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Guarded Google Play track promotion")
    parser.add_argument("--credentials", type=pathlib.Path, required=True)
    parser.add_argument("--track", required=True)
    parser.add_argument("--version-code", type=int, default=1)
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
