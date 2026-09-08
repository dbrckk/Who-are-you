import json
import tempfile
import unittest
from pathlib import Path

from play_preflight import ROOT, run


class M54PlayPreflightTest(unittest.TestCase):
    def test_repository_preflight_is_ready_without_external_inputs(self):
        report = run(ROOT, None, strict_public=False)
        self.assertTrue(report["repository_ready"])
        self.assertTrue(report["public_release_ready"])
        self.assertTrue(all(check["ok"] for check in report["checks"]))

    def test_strict_public_preflight_requires_external_inputs(self):
        report = run(ROOT, None, strict_public=True)
        self.assertTrue(report["repository_ready"])
        self.assertFalse(report["public_release_ready"])
        self.assertIn("public release input file was not provided", report["external_inputs"]["errors"])

    def test_strict_public_preflight_rejects_template_placeholders(self):
        template = ROOT / "docs/public-release-inputs.template.json"
        report = run(ROOT, template, strict_public=True)
        self.assertFalse(report["public_release_ready"])
        self.assertTrue(report["external_inputs"]["errors"])

    def test_strict_public_preflight_accepts_complete_contract_shape(self):
        payload = {
            "developer_display_name": "Who Are You?",
            "support_email": "support@example.org",
            "target_audience": "13+",
            "privacy_policy_url": "https://example.org/privacy/",
            "challenge_domain": "example.org",
            "play_app_signing_sha256": ":".join(["AB"] * 32),
            "remove_ads_product_id": "remove_ads_lifetime",
            "remove_ads_target_price_eur": 1.99,
            "production_telemetry_enabled": False,
            "production_telemetry_retention_days": None,
            "production_telemetry_deletion_contact": None,
        }
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "inputs.json"
            path.write_text(json.dumps(payload), encoding="utf-8")
            report = run(ROOT, path, strict_public=True)
        self.assertTrue(report["public_release_ready"])
        self.assertFalse(report["external_inputs"]["errors"])


if __name__ == "__main__":
    unittest.main()
