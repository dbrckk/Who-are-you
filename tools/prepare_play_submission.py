#!/usr/bin/env python3
import argparse
import json
import re
from pathlib import Path
from urllib.parse import urlparse

PACKAGE_NAME = "com.whoareyou.app"
PLACEHOLDER_MARKERS = ("REPLACE", "example.com", "REPLACE_BEFORE", "REPLACE_AFTER")
EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")
ADMOB_APP_RE = re.compile(r"^ca-app-pub-\d{16}~\d{10}$")
ADMOB_UNIT_RE = re.compile(r"^ca-app-pub-\d{16}/\d{10}$")
SHA256_RE = re.compile(r"^(?:[0-9A-Fa-f]{2}:){31}[0-9A-Fa-f]{2}$")


class InputError(ValueError):
    pass


def _required_string(payload, key):
    value = payload.get(key)
    if not isinstance(value, str) or not value.strip():
        raise InputError(f"{key} is required")
    value = value.strip()
    if any(marker.lower() in value.lower() for marker in PLACEHOLDER_MARKERS):
        raise InputError(f"{key} still contains a placeholder")
    return value


def validate_inputs(payload):
    support_email = _required_string(payload, "supportEmail")
    if not EMAIL_RE.fullmatch(support_email):
        raise InputError("supportEmail is not a valid email address")

    privacy_url = _required_string(payload, "privacyPolicyUrl")
    parsed = urlparse(privacy_url)
    if parsed.scheme != "https" or not parsed.netloc:
        raise InputError("privacyPolicyUrl must be a public HTTPS URL")

    admob = payload.get("admob")
    if not isinstance(admob, dict):
        raise InputError("admob object is required")
    app_id = _required_string(admob, "appId")
    interstitial_id = _required_string(admob, "interstitialId")
    if not ADMOB_APP_RE.fullmatch(app_id):
        raise InputError("admob.appId is malformed")
    if not ADMOB_UNIT_RE.fullmatch(interstitial_id):
        raise InputError("admob.interstitialId is malformed")

    fingerprint = _required_string(payload, "playAppSigningSha256").upper()
    if not SHA256_RE.fullmatch(fingerprint):
        raise InputError("playAppSigningSha256 must contain 32 colon-separated hex bytes")

    billing = payload.get("billing")
    product = billing.get("remove_ads_lifetime") if isinstance(billing, dict) else None
    if not isinstance(product, dict):
        raise InputError("billing.remove_ads_lifetime is required")
    if product.get("active") is not True:
        raise InputError("remove_ads_lifetime must be active in Play Console")
    if product.get("priceConfiguredInPlayConsole") is not True:
        raise InputError("remove_ads_lifetime price must be configured in Play Console")

    tester_group = _required_string(payload, "testerGroup")

    return {
        "supportEmail": support_email,
        "privacyPolicyUrl": privacy_url,
        "admobAppId": app_id,
        "admobInterstitialId": interstitial_id,
        "playAppSigningSha256": fingerprint,
        "testerGroup": tester_group,
    }


def render_assetlinks(values):
    return json.dumps([
        {
            "relation": ["delegate_permission/common.handle_all_urls"],
            "target": {
                "namespace": "android_app",
                "package_name": PACKAGE_NAME,
                "sha256_cert_fingerprints": [values["playAppSigningSha256"]],
            },
        }
    ], indent=2) + "\n"


def render_gradle_properties(values):
    return (
        f"WHO_ARE_YOU_ADMOB_APP_ID={values['admobAppId']}\n"
        f"WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID={values['admobInterstitialId']}\n"
        "# WHO_ARE_YOU_TELEMETRY_ENDPOINT intentionally omitted for first Internal testing candidate.\n"
    )


def render_privacy_policy(template, support_email):
    marker = "Before public release, replace this section with the developer support/contact address used in the Google Play listing."
    if marker not in template:
        raise InputError("privacy policy contact placeholder was not found")
    return template.replace(marker, f"Contact: {support_email}")


def render_summary(values):
    return (
        "# Generated Play Internal testing pack\n\n"
        f"- Package: `{PACKAGE_NAME}`\n"
        f"- Support email: `{values['supportEmail']}`\n"
        f"- Privacy policy URL: `{values['privacyPolicyUrl']}`\n"
        f"- Tester group: `{values['testerGroup']}`\n"
        "- Billing product `remove_ads_lifetime`: active + priced\n"
        "- Custom telemetry: disabled by default\n\n"
        "## Generated files\n\n"
        "- `assetlinks.json` — publish at `/.well-known/assetlinks.json`.\n"
        "- `production-gradle.properties` — pass these values only to the production Play build.\n"
        "- `privacy-policy-final.md` — publish at the configured HTTPS privacy-policy URL.\n"
    )


def generate(input_path, repo_root, output_dir):
    payload = json.loads(Path(input_path).read_text(encoding="utf-8"))
    values = validate_inputs(payload)
    template = (Path(repo_root) / "docs/privacy-policy.md").read_text(encoding="utf-8")
    out = Path(output_dir)
    out.mkdir(parents=True, exist_ok=True)
    (out / "assetlinks.json").write_text(render_assetlinks(values), encoding="utf-8")
    (out / "production-gradle.properties").write_text(render_gradle_properties(values), encoding="utf-8")
    (out / "privacy-policy-final.md").write_text(render_privacy_policy(template, values["supportEmail"]), encoding="utf-8")
    (out / "submission-summary.md").write_text(render_summary(values), encoding="utf-8")
    return values


def main():
    parser = argparse.ArgumentParser(description="Validate external Play inputs and generate the production submission pack.")
    parser.add_argument("--input", required=True)
    parser.add_argument("--repo-root", default=".")
    parser.add_argument("--output", default="build/play-submission")
    args = parser.parse_args()
    try:
        values = generate(args.input, args.repo_root, args.output)
    except (InputError, json.JSONDecodeError, OSError) as exc:
        raise SystemExit(f"Play submission input error: {exc}")
    print(f"Play submission pack generated for {PACKAGE_NAME} using {values['supportEmail']}")


if __name__ == "__main__":
    main()
