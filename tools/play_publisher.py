#!/usr/bin/env python3
"""Google Play Internal-track publishing helper.

The module keeps request/payload construction deterministic and testable without
Google credentials. Network calls are only made by publish_bundle().
"""
from __future__ import annotations

import argparse
import json
import pathlib
import sys
from dataclasses import dataclass
from typing import Any

PACKAGE_NAME = "com.whoareyou.app"
TRACK = "internal"
VERSION_CODE = 1
VERSION_NAME = "0.1.0"
API_ROOT = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications"
UPLOAD_ROOT = "https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"


@dataclass(frozen=True)
class ReleaseConfig:
    package_name: str = PACKAGE_NAME
    track: str = TRACK
    version_code: int = VERSION_CODE
    version_name: str = VERSION_NAME

    def validate(self) -> None:
        if self.package_name != PACKAGE_NAME:
            raise ValueError(f"Unexpected package name: {self.package_name}")
        if self.track != TRACK:
            raise ValueError(f"Only the {TRACK!r} track is allowed")
        if self.version_code != VERSION_CODE:
            raise ValueError(f"Expected versionCode {VERSION_CODE}")
        if self.version_name != VERSION_NAME:
            raise ValueError(f"Expected versionName {VERSION_NAME}")


def release_payload(version_code: int = VERSION_CODE, status: str = "draft") -> dict[str, Any]:
    if status not in {"draft", "completed"}:
        raise ValueError("Release status must be 'draft' or 'completed'")
    return {
        "track": TRACK,
        "releases": [
            {
                "name": VERSION_NAME,
                "versionCodes": [str(version_code)],
                "status": status,
            }
        ],
    }


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
    except ImportError as exc:  # pragma: no cover - exercised only in live workflow
        raise RuntimeError("Install google-auth and requests before publishing") from exc

    credentials = service_account.Credentials.from_service_account_file(
        str(credentials_path), scopes=[SCOPE]
    )
    return AuthorizedSession(credentials)


def publish_bundle(
    bundle: pathlib.Path,
    credentials_path: pathlib.Path,
    *,
    release_status: str,
    commit: bool,
) -> dict[str, Any]:
    config = ReleaseConfig()
    config.validate()
    if not bundle.is_file() or bundle.stat().st_size == 0:
        raise ValueError(f"Bundle is missing or empty: {bundle}")
    load_service_account(credentials_path)

    session = _authorized_session(credentials_path)
    insert_url = f"{API_ROOT}/{config.package_name}/edits"
    response = session.post(insert_url, json={}, timeout=60)
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

    track_url = endpoint(config.package_name, edit_id, f"tracks/{config.track}")
    track_update = session.put(
        track_url,
        json=release_payload(uploaded_version, release_status),
        timeout=60,
    )
    track_update.raise_for_status()

    validate = session.post(
        endpoint(config.package_name, edit_id, "validate"), json={}, timeout=60
    )
    validate.raise_for_status()

    result: dict[str, Any] = {
        "editId": edit_id,
        "packageName": config.package_name,
        "track": config.track,
        "versionCode": uploaded_version,
        "releaseStatus": release_status,
        "committed": False,
    }

    if commit:
        commit_response = session.post(
            endpoint(config.package_name, edit_id, "commit"), json={}, timeout=60
        )
        commit_response.raise_for_status()
        result["committed"] = True

    return result


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--bundle", type=pathlib.Path)
    parser.add_argument("--credentials", type=pathlib.Path)
    parser.add_argument("--status", choices=("draft", "completed"), default="draft")
    parser.add_argument("--commit", action="store_true")
    parser.add_argument("--validate-only", action="store_true")
    args = parser.parse_args(argv)

    ReleaseConfig().validate()
    if args.validate_only:
        print(json.dumps({"config": ReleaseConfig().__dict__, "payload": release_payload()}, indent=2))
        return 0

    if args.bundle is None or args.credentials is None:
        parser.error("--bundle and --credentials are required unless --validate-only is used")

    result = publish_bundle(
        args.bundle,
        args.credentials,
        release_status=args.status,
        commit=args.commit,
    )
    print(json.dumps(result, indent=2, sort_keys=True))
    return 0


if __name__ == "__main__":
    sys.exit(main())
