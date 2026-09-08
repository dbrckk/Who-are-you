#!/usr/bin/env python3
import argparse
import json
from pathlib import Path

from validate_public_release_inputs import load as load_public_inputs
from validate_public_release_inputs import validate as validate_public_inputs

ROOT = Path(__file__).resolve().parents[1]


def check_repository(root: Path) -> list[dict]:
    checks = []

    def add(name: str, ok: bool, detail: str):
        checks.append({"name": name, "ok": bool(ok), "detail": detail})

    gradle = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
    manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    ad_manager = (root / "app/src/main/java/com/whoareyou/app/AdManager.kt").read_text(encoding="utf-8")
    billing = (root / "app/src/main/java/com/whoareyou/app/BillingManager.kt").read_text(encoding="utf-8")
    privacy = (root / "docs/privacy-policy.md").read_text(encoding="utf-8")
    data_safety = (root / "docs/play-data-safety.md").read_text(encoding="utf-8")
    app_links = (root / "docs/APP_LINKS.md").read_text(encoding="utf-8")

    add("release_minification", "isMinifyEnabled = true" in gradle, "R8 enabled for release")
    add("release_resource_shrinking", "isShrinkResources = true" in gradle, "resource shrinking enabled")
    add("cleartext_disabled", 'android:usesCleartextTraffic="false"' in manifest, "cleartext traffic disabled")
    add("backup_disabled", 'android:allowBackup="false"' in manifest, "Android backup disabled for local profile data")
    add("ump_gate", "canRequestAds()" in ad_manager, "AdMob requests gated by UMP consent state")
    add("ump_privacy_options", "showPrivacyOptionsForm" in ad_manager, "privacy options form available")
    add("billing_product", 'REMOVE_ADS_PRODUCT_ID = "remove_ads_lifetime"' in billing, "lifetime product ID locked")
    add("billing_acknowledge", "acknowledgePurchase" in billing, "purchases acknowledged")
    add("privacy_policy", "Google Mobile Ads" in privacy and "Data deletion" in privacy, "privacy policy covers ads and deletion")
    add("data_safety", "device and account identifiers" in data_safety and "IP address" in data_safety, "Data Safety worksheet covers GMA disclosures")
    add("app_links_dependency", "https://dbrckk.github.io/.well-known/assetlinks.json" in app_links, "root-domain DAL dependency documented")

    return checks


def run(root: Path, public_inputs: Path | None, strict_public: bool) -> dict:
    checks = check_repository(root)
    repo_ready = all(item["ok"] for item in checks)

    external = {
        "provided": public_inputs is not None,
        "strict": strict_public,
        "errors": [],
        "ready": False,
    }
    if public_inputs is not None:
        try:
            payload = load_public_inputs(public_inputs)
            external["errors"] = validate_public_inputs(payload, strict=strict_public)
        except (OSError, ValueError, json.JSONDecodeError) as exc:
            external["errors"] = [str(exc)]
        external["ready"] = not external["errors"]
    elif strict_public:
        external["errors"] = ["public release input file was not provided"]

    public_ready = repo_ready and external["ready"] if strict_public else repo_ready
    return {
        "repository_ready": repo_ready,
        "public_release_ready": public_ready,
        "checks": checks,
        "external_inputs": external,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Autonomous Google Play release readiness preflight")
    parser.add_argument("--repo-root", type=Path, default=ROOT)
    parser.add_argument("--public-inputs", type=Path)
    parser.add_argument("--strict-public", action="store_true")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()

    report = run(args.repo_root, args.public_inputs, args.strict_public)
    encoded = json.dumps(report, indent=2, sort_keys=True)
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(encoded + "\n", encoding="utf-8")
    print(encoded)
    return 0 if report["public_release_ready"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
