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
        self.assertEqual(config.track, "qa")
        self.assertEqual(config.version_code, 1)
        self.assertEqual(config.version_name, "0.1.0")

    def test_default_release_payload_uses_internal_qa_track(self):
        payload = play_publisher.release_payload()
        self.assertEqual(payload["track"], "qa")
        release = payload["releases"][0]
        self.assertEqual(release["name"], "0.1.0")
        self.assertEqual(release["versionCodes"], ["1"])
        self.assertEqual(release["status"], "draft")

    def test_open_testing_track_is_supported(self):
        payload = play_publisher.release_payload(track="beta", status="completed")
        self.assertEqual(payload["track"], "beta")
        self.assertEqual(payload["releases"][0]["status"], "completed")

    def test_custom_closed_track_is_supported(self):
        payload = play_publisher.release_payload(track="closed-alpha", status="draft")
        self.assertEqual(payload["track"], "closed-alpha")

    def test_production_requires_explicit_confirmation(self):
        with self.assertRaises(ValueError):
            play_publisher.release_payload(track="production", status="completed")

    def test_production_staged_rollout_is_supported_when_confirmed(self):
        payload = play_publisher.release_payload(
            track="production",
            status="inProgress",
            user_fraction=0.1,
            allow_production=True,
        )
        release = payload["releases"][0]
        self.assertEqual(release["userFraction"], 0.1)
        self.assertEqual(release["status"], "inProgress")

    def test_production_in_progress_requires_fraction(self):
        with self.assertRaises(ValueError):
            play_publisher.release_payload(
                track="production",
                status="inProgress",
                allow_production=True,
            )

    def test_non_production_rollout_fraction_is_rejected(self):
        with self.assertRaises(ValueError):
            play_publisher.release_payload(
                track="beta", status="inProgress", user_fraction=0.1
            )

    def test_invalid_status_is_rejected(self):
        with self.assertRaises(ValueError):
            play_publisher.release_payload(status="unknown")

    def test_only_locked_package_is_allowed(self):
        with self.assertRaises(ValueError):
            play_publisher.ReleaseConfig(package_name="example.invalid").validate()

    def test_malformed_track_is_rejected(self):
        with self.assertRaises(ValueError):
            play_publisher.release_payload(track="../../bad")

    def test_service_account_validation_rejects_missing_fields(self):
        with tempfile.TemporaryDirectory() as directory:
            path = pathlib.Path(directory) / "service-account.json"
            path.write_text(json.dumps({"type": "service_account"}), encoding="utf-8")
            with self.assertRaises(ValueError):
                play_publisher.load_service_account(path)

    def test_endpoint_shape(self):
        self.assertEqual(
            play_publisher.endpoint("com.whoareyou.app", "abc", "tracks/qa"),
            "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/com.whoareyou.app/edits/abc/tracks/qa",
        )


if __name__ == "__main__":
    unittest.main()
