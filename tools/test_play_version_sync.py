from pathlib import Path
import unittest

import play_publisher

ROOT = Path(__file__).resolve().parents[1]
BUILD_GRADLE = ROOT / "app" / "build.gradle.kts"
PROMOTER = ROOT / "tools" / "play_promoter.py"


class PlayVersionSyncTest(unittest.TestCase):
    def test_publisher_version_is_loaded_from_android_gradle(self):
        code, name = play_publisher.load_android_version(BUILD_GRADLE)
        self.assertEqual(code, play_publisher.VERSION_CODE)
        self.assertEqual(name, play_publisher.VERSION_NAME)
        self.assertEqual(code, play_publisher.ReleaseConfig().version_code)
        self.assertEqual(name, play_publisher.ReleaseConfig().version_name)

    def test_promoter_does_not_pin_an_obsolete_version_code(self):
        source = PROMOTER.read_text(encoding="utf-8")
        self.assertIn('default=ReleaseConfig().version_code', source)
        self.assertNotIn('default=1)', source)


if __name__ == "__main__":
    unittest.main()
