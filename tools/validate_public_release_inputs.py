#!/usr/bin/env python3
import argparse
import json
import re
from pathlib import Path

PLACEHOLDER_VALUES = {
    "REQUIRED",
    "REQUIRED_AFTER_PLAY_APP_SIGNING",
    "TODO",
    "TBD",
    "",
}

SHA256_RE = re.compile(r"^(?:[0-9A-F]{2}:){31}[0-9A-F]{2}$")
EMAIL_RE = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


def load(path: Path) -> dict:
    with path.open(encoding="utf-8") as handle:
        payload = json.load(handle)
    if not isinstance(payload, dict):
        raise ValueError("release input file must contain one JSON object")
    return payload


def validate(payload: dict, strict: bool = True) -> list[str]:
    errors: list[str] = []

    required = (
        "developer_display_name",
        "support_email",
        "target_audience",
        "privacy_policy_url",
        "challenge_domain",
        "play_app_signing_sha256",
        "remove_ads_product_id",
        "remove_ads_target_price_eur",
        "production_telemetry_enabled",
    )
    for key in required:
        if key not in payload:
            errors.append(f"missing key: {key}")

    if errors:
        return errors

    if strict:
        for key in ("developer_display_name", "support_email", "target_audience", "play_app_signing_sha256"):
            if str(payload[key]).strip() in PLACEHOLDER_VALUES:
                errors.append(f"unresolved public release field: {key}")

    support_email = str(payload["support_email"]).strip()
    if support_email not in PLACEHOLDER_VALUES and not EMAIL_RE.fullmatch(support_email):
        errors.append("support_email must be a valid email address")

    privacy_url = str(payload["privacy_policy_url"]).strip()
    if not privacy_url.startswith("https://"):
        errors.append("privacy_policy_url must use HTTPS")

    domain = str(payload["challenge_domain"]).strip()
    if not domain or "/" in domain or ":" in domain:
        errors.append("challenge_domain must be a bare hostname")

    fingerprint = str(payload["play_app_signing_sha256"]).strip()
    if fingerprint not in PLACEHOLDER_VALUES and not SHA256_RE.fullmatch(fingerprint):
        errors.append("play_app_signing_sha256 must be an uppercase colon-separated SHA-256 fingerprint")

    if payload["remove_ads_product_id"] != "remove_ads_lifetime":
        errors.append("remove_ads_product_id must stay remove_ads_lifetime")

    try:
        target_price = float(payload["remove_ads_target_price_eur"])
    except (TypeError, ValueError):
        errors.append("remove_ads_target_price_eur must be numeric")
    else:
        if target_price <= 0:
            errors.append("remove_ads_target_price_eur must be positive")

    telemetry_enabled = payload["production_telemetry_enabled"]
    if not isinstance(telemetry_enabled, bool):
        errors.append("production_telemetry_enabled must be boolean")
    elif telemetry_enabled:
        retention = payload.get("production_telemetry_retention_days")
        contact = payload.get("production_telemetry_deletion_contact")
        if not isinstance(retention, int) or retention <= 0:
            errors.append("telemetry enabled requires positive production_telemetry_retention_days")
        if not isinstance(contact, str) or not contact.strip():
            errors.append("telemetry enabled requires production_telemetry_deletion_contact")

    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate public Google Play release inputs")
    parser.add_argument("path", type=Path)
    parser.add_argument("--allow-placeholders", action="store_true")
    args = parser.parse_args()

    errors = validate(load(args.path), strict=not args.allow_placeholders)
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1
    print("Public release inputs validated")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
