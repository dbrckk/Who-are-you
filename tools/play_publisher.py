#!/usr/bin/env python3
"""Google Play publishing helper with explicit track and rollout safeguards.

Request/payload construction stays deterministic and testable without Google
credentials. Network calls are made only by publish_bundle(). Production is
opt-in and requires an explicit confirmation flag.
"""
from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys
from dataclasses import dataclass
from typing import Any

PACKAGE_NAME = "com.whoareyou.app"
INTERNAL_TRACK = "qa"
OPEN_TEST_TRACK = "beta"
PRODUCTION_TRACK = "production"
VERSION_CODE = 1
VERSION_NAME = "0.1.0"
API_ROOT = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications"
UPLOAD_ROOT = "https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"
TRACK_RE = re.compile(r"^[a-zA-Z0-9._-]{1,80}$")
RELEASE_STATUSES = {"draft", "inProgress", "halted", "completed"}


@dataclass(frozen=True)
class ReleaseConfig:
    package_name: str = PACKAGE_NAME
    track: str = INTERNAL_TRACK
    version_code: int = VERSION_CODE
    version_name: str = VERSION_NAME

    def validate(self, *, allow_production: bool = False) -> None:
        if self.package_name != PACKAGE_NAME:
            raise ValueError(f"Unexpected package name: {self.package_name}")
        validate_track(self.track, allow_production=allow_production)
        if self.version_code != VERSION_CODE:
            raise ValueError(f"Expected versionCode {VERSION_CODE}")
        if self.version_name != VERSION_NAME:
            raise ValueError(f"Expected versionName {VERSION_NAME}")


def validate_track(track: str, *, allow_production: bool = False) -> str:
    if not isinstance(track, str) or not TRACK_RE.fullmatch(track):
        raise ValueError("Track must be a simple Play track identifier")
    if track == PRODUCTION_TRACK and not allow_production:
        raise ValueError("Production publishing requires explicit confirmation")
    return track


def release_payload(
    version_code: int = VERSION_CODE,
    *,
    track: str = INTERNAL_TRACK,
    status: str = "draft",
    user_fraction: float | None = None,
    allow_production: bool = False,
) -> dict[str, Any]:
    validate_track(track, allow_production=allow_production)
    if status not in RELEASE_STATUSES:
        raise ValueError(f"Unsupported release status: {status}")
    if user_fraction is not None:
        if status not in {"inProgress", "halted"}:
            raise ValueError("userFraction is only valid for inProgress or halted releases")
        if not 0 < user_fraction < 1:
            raise ValueError("userFraction must be greater than 0 and less than 1")
        if track != PRODUCTION_TRACK:
            raise ValueError("Staged rollout is restricted to production in this publisher")
    if track == PRODUCTION_TRACK and status == "inProgress" and user_fraction is None:
        raise ValueError("Production inProgress releases require a userFraction")

    release: dict[str, Any] = {
        "name": VERSION_NAME,
        "versionCodes": [str(version_code)],
        "status": status,
    }
    if user_fraction is not None:
        release["userFraction"] = user_fraction
    return {"track": track, "releases": [release]}


def endpoint(package_name: str, edit_id: str, resource: str) -> str:
    return f"{API_ROOT}/{package_name}/edits/{edit_id}/{resource}"


def load_service_account(path: pathlib.Path) -> dict[str, Any]:
    data = json.loads(path.read_text(encoding="utf-8"))
    required = {"type", "client_email", "private_key", "token_uri"}
    missing = sorted(required - data.keys())
    if missing:
        raise ValueError(f"Service-account JSON is missing: {', '.join(missing)}")
    if data.get("type") != "service_account":
        raise ValueError("Credentials must be a Google service account")
    return data


def _authorized_session(credentials_path: pathlib.Path):
    try:
        from google.auth.transport.requests import AuthorizedSession
        from google.oauth2 import service_account
    except ImportError as exc:  # pragma: no cover
        raise RuntimeError("Install google-auth and requests before publishing") from exc

    credentials = service_account.Credentials.from_service_account_file(
        str(credentials_path), scopes=[SCOPE]
    )
    return AuthorizedSession(credentials)


def publish_bundle(
    bundle: pathlib.Path,
    credentials_path: pathlib.Path,
    *,
    track: str,
    release_status: str,
    user_fraction: float | None,
    commit: bool,
    allow_production: bool,
) -> dict[str, Any]:
    config = ReleaseConfig(track=track)
    config.validate(allow_production=allow_production)
    payload = release_payload(
        config.version_code,
        track=track,
        status=release_status,
        user_fraction=user_fraction,
        allow_production=allow_production,
    )
    if not bundle.is_file() or bundle.stat().st_size == 0:
        raise ValueError(f"Bundle is missing or empty: {bundle}")
    load_service_account(credentials_path)

    session = _authorized_session(credentials_path)
    response = session.post(f"{API_ROOT}/{config.package_name}/edits", json={}, timeout=60)
    response.raise_for_status()
    edit_id = response.json()["id"]

    upload_url = f"{UPLOAD_ROOT}/{config.package_name}/edits/{edit_id}/bundles"
    with bundle.open("rb") as handle:
        upload = session.post(
            upload_url,
            params={"uploadType": "media"},
            headers={"Content-Type": "application/octet-stream"},
            data=handle,
            timeout=180,
        )
    upload.raise_for_status()
    uploaded_version = int(upload.json()["versionCode"])
    if uploaded_version != config.version_code:
        raise RuntimeError(
            f"Play returned versionCode {uploaded_version}; expected {config.version_code}"
        )

    track_url = endpoint(config.package_name, edit_id, f"tracks/{track}")
    track_update = session.put(track_url, json=payload, timeout=60)
    track_update.raise_for_status()

    validate = session.post(endpoint(config.package_name, edit_id, "validate"), json={}, timeout=60)
    validate.raise_for_status()

    result: dict[str, Any] = {
        "editId": edit_id,
        "packageName": config.package_name,
        "track": track,
        "versionCode": uploaded_version,
        "releaseStatus": release_status,
        "userFraction": user_fraction,
        "committed": False,
    }
    if commit:
        commit_response = session.post(endpoint(config.package_name, edit_id, "commit"), json={}, timeout=60)
        commit_response.raise_for_status()
        result["committed"] = True
    return result


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--bundle", type=pathlib.Path)
    parser.add_argument("--credentials", type=pathlib.Path)
    parser.add_argument("--track", default=INTERNAL_TRACK)
    parser.add_argument("--status", choices=tuple(sorted(RELEASE_STATUSES)), default="draft")
    parser.add_argument("--user-fraction", type=float)
    parser.add_argument("--confirm-production", action="store_true")
    parser.add_argument("--commit", action="store_true")
    parser.add_argument("--validate-only", action="store_true")
    args = parser.parse_args(argv)

    config = ReleaseConfig(track=args.track)
    config.validate(allow_production=args.confirm_production)
    payload = release_payload(
        track=args.track,
        status=args.status,
        user_fraction=args.user_fraction,
        allow_production=args.confirm_production,
    )
    if args.validate_only:
        print(json.dumps({"config": config.__dict__, "payload": payload}, indent=2))
        return 0

    if args.bundle is None or args.credentials is None:
        parser.error("--bundle and --credentials are required unless --validate-only is used")

    result = publish_bundle(
        args.bundle,
        args.credentials,
        track=args.track,
        release_status=args.status,
        user_fraction=args.user_fraction,
        commit=args.commit,
        allow_production=args.confirm_production,
    )
    print(json.dumps(result, indent=2, sort_keys=True))
    return 0


if __name__ == "__main__":
    sys.exit(main())
