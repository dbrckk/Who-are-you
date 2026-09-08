from pathlib import Path
import json
import unittest

from validate_public_release_inputs import validate

ROOT = Path(__file__).resolve().parents[1]


class M53PublicationContractTest(unittest.TestCase):
    def test_privacy_page_exists_and_is_explicitly_pre_release(self):
        html = (ROOT / "docs/privacy/index.html").read_text(encoding="utf-8")
        self.assertIn("Privacy Policy", html)
        self.assertIn("Pre-release notice", html)
        self.assertIn("REQUIRED BEFORE PUBLIC RELEASE", html)
        self.assertIn("Google Mobile Ads", html)
        self.assertIn("remove_ads_lifetime", html)

    def test_data_safety_tracks_current_admob_disclosures(self):
        text = (ROOT / "docs/play-data-safety.md").read_text(encoding="utf-8")
        for expected in (
            "IP address",
            "user product interactions",
            "diagnostic information",
            "device and account identifiers",
            "Advertising ID",
            "encrypted in transit",
        ):
            self.assertIn(expected, text)
        self.assertIn("25.4.0", text)

    def test_public_release_inputs_contract_is_safe(self):
        payload = json.loads(
            (ROOT / "docs/public-release-inputs.template.json").read_text(encoding="utf-8")
        )
        self.assertEqual(payload["remove_ads_product_id"], "remove_ads_lifetime")
        self.assertEqual(payload["remove_ads_target_price_eur"], 1.99)
        self.assertFalse(payload["production_telemetry_enabled"])
        self.assertTrue(payload["privacy_policy_url"].startswith("https://"))
        self.assertEqual(payload["challenge_domain"], "dbrckk.github.io")
        self.assertEqual(payload["developer_display_name"], "REQUIRED")
        self.assertEqual(payload["support_email"], "REQUIRED")

    def test_strict_public_release_gate_rejects_template_placeholders(self):
        payload = json.loads(
            (ROOT / "docs/public-release-inputs.template.json").read_text(encoding="utf-8")
        )
        errors = validate(payload, strict=True)
        self.assertTrue(any("developer_display_name" in error for error in errors))
        self.assertTrue(any("support_email" in error for error in errors))
        self.assertTrue(any("target_audience" in error for error in errors))
        self.assertTrue(any("play_app_signing_sha256" in error for error in errors))

    def test_app_links_docs_explain_root_domain_dependency(self):
        text = (ROOT / "docs/APP_LINKS.md").read_text(encoding="utf-8")
        self.assertIn("https://dbrckk.github.io/.well-known/assetlinks.json", text)
        self.assertIn("App signing key certificate", text)
        self.assertIn("project URL", text)


if __name__ == "__main__":
    unittest.main()
