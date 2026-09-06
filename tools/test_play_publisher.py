import json
import pathlib
import tempfile
import unittest

import play_publisher


class PlayPublisherTest(unittest.TestCase):
    def test_locked_release_config(self):
        config = play_publisher.ReleaseConfig()
        config.validate()
        self.assertEqual(config.package_name, "com.whoareyou.app")
        self.assertEqual(config.track, "internal")
        self.assertEqual(config.version_code, 1)
        self.assertEqual(config.version_name, "0.1.0")

    def test_release_payload_uses_internal_and_version_code(self):
        payload = play_publisher.release_payload()
        self.assertEqual(payload["track"], "internal")
        release = payload["releases"][0]
        self.assertEqual(release["name"], "0.1.0")
        self.assertEqual(release["versionCodes"], ["1"])
        self.assertEqual(release["status"], "draft")

    def test_completed_status_is_explicitly_supported(self):
        payload = play_publisher.release_payload(status="completed")
        self.assertEqual(payload["releases"][0]["status"], "completed")

    def test_invalid_status_is_rejected(self):
        with self.assertRaises(ValueError):
            play_publisher.release_payload(status="inProgress")

    def test_only_locked_package_is_allowed(self):
        with self.assertRaises(ValueError):
            play_publisher.ReleaseConfig(package_name="example.invalid").validate()

    def test_service_account_validation_rejects_missing_fields(self):
        with tempfile.TemporaryDirectory() as directory:
            path = pathlib.Path(directory) / "service-account.json"
            path.write_text(json.dumps({"type": "service_account"}), encoding="utf-8")
            with self.assertRaises(ValueError):
                play_publisher.load_service_account(path)

    def test_endpoint_shape(self):
        self.assertEqual(
            play_publisher.endpoint("com.whoareyou.app", "abc", "tracks/internal"),
            "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/com.whoareyou.app/edits/abc/tracks/internal",
        )


if __name__ == "__main__":
    unittest.main()
