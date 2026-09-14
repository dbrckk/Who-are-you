from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class PlayReleaseSafetyContractTest(unittest.TestCase):
    def setUp(self):
        self.gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")

    def test_upload_key_password_can_fall_back_to_keystore_password(self):
        self.assertIn(
            "val effectiveUploadKeyPassword = uploadKeyPassword?.takeIf { it.isNotBlank() } ?: uploadKeystorePassword",
            self.gradle,
        )
        self.assertIn("keyPassword = effectiveUploadKeyPassword", self.gradle)
        self.assertNotIn(
            'playRelease requires WHO_ARE_YOU_UPLOAD_KEY_PASSWORD',
            self.gradle,
        )

    def test_standard_release_disables_external_services(self):
        release_block = self.gradle.split("release {", 1)[1].split("}", 1)[0]
        self.assertIn(
            'buildConfigField("boolean", "EXTERNAL_SERVICES_ENABLED", "false")',
            release_block,
        )

    def test_play_release_explicitly_reenables_external_services(self):
        play_block = self.gradle.split('create("playRelease") {', 1)[1]
        self.assertIn(
            'buildConfigField("boolean", "EXTERNAL_SERVICES_ENABLED", "true")',
            play_block,
        )


if __name__ == "__main__":
    unittest.main()
