import json
import tempfile
import unittest
from pathlib import Path

from prepare_play_submission import InputError, generate, validate_inputs


VALID = {
    "supportEmail": "support@whoareyou.app",
    "privacyPolicyUrl": "https://whoareyou.app/privacy",
    "admob": {
        "appId": "ca-app-pub-1234567890123456~1234567890",
        "interstitialId": "ca-app-pub-1234567890123456/1234567890"
    },
    "playAppSigningSha256": ":".join(["AB"] * 32),
    "billing": {
        "remove_ads_lifetime": {
            "active": True,
            "priceConfiguredInPlayConsole": True
        }
    },
    "testerGroup": "internal-testers@whoareyou.app"
}


class PlaySubmissionGeneratorTest(unittest.TestCase):
    def test_valid_inputs_generate_all_files(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / "docs/privacy").mkdir(parents=True)
            (root / "docs/privacy-policy.md").write_text(
                "# Policy\n\n**REQUIRED BEFORE PUBLIC RELEASE:** replace this line with the public developer/privacy support email or other valid support contact used for the Google Play listing.\n",
                encoding="utf-8",
            )
            (root / "docs/privacy/index.html").write_text(
                '<p class="notice"><strong>Pre-release notice:</strong> the privacy terms below reflect the current application architecture. A public developer/privacy support contact must be inserted before the first public Google Play release.</p>\n'
                '<p><strong>REQUIRED BEFORE PUBLIC RELEASE:</strong> insert the public developer/privacy support email or support contact used for the Google Play listing.</p>\n',
                encoding="utf-8",
            )
            input_path = root / "input.json"
            input_path.write_text(json.dumps(VALID), encoding="utf-8")
            out = root / "out"
            values = generate(input_path, root, out)

            self.assertEqual(values["supportEmail"], "support@whoareyou.app")
            self.assertTrue((out / "assetlinks.json").is_file())
            self.assertTrue((out / "production-gradle.properties").is_file())
            self.assertTrue((out / "privacy-policy-final.md").is_file())
            self.assertTrue((out / "privacy-policy-final.html").is_file())
            self.assertTrue((out / "submission-summary.md").is_file())
            self.assertIn("com.whoareyou.app", (out / "assetlinks.json").read_text())
            self.assertIn("support@whoareyou.app", (out / "privacy-policy-final.md").read_text())
            final_html = (out / "privacy-policy-final.html").read_text()
            self.assertIn("mailto:support@whoareyou.app", final_html)
            self.assertNotIn("Pre-release notice", final_html)
            self.assertNotIn("REQUIRED BEFORE PUBLIC RELEASE", final_html)
            self.assertNotIn("WHO_ARE_YOU_TELEMETRY_ENDPOINT=", (out / "production-gradle.properties").read_text())

    def test_placeholder_is_rejected(self):
        payload = dict(VALID)
        payload["supportEmail"] = "REPLACE_BEFORE_SUBMISSION"
        with self.assertRaises(InputError):
            validate_inputs(payload)

    def test_non_https_privacy_url_is_rejected(self):
        payload = dict(VALID)
        payload["privacyPolicyUrl"] = "http://whoareyou.app/privacy"
        with self.assertRaises(InputError):
            validate_inputs(payload)

    def test_malformed_admob_id_is_rejected(self):
        payload = json.loads(json.dumps(VALID))
        payload["admob"]["appId"] = "not-an-admob-id"
        with self.assertRaises(InputError):
            validate_inputs(payload)

    def test_malformed_fingerprint_is_rejected(self):
        payload = dict(VALID)
        payload["playAppSigningSha256"] = "AA:BB"
        with self.assertRaises(InputError):
            validate_inputs(payload)

    def test_inactive_billing_product_is_rejected(self):
        payload = json.loads(json.dumps(VALID))
        payload["billing"]["remove_ads_lifetime"]["active"] = False
        with self.assertRaises(InputError):
            validate_inputs(payload)


if __name__ == "__main__":
    unittest.main()
