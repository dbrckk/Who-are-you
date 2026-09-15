from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = (ROOT / 'baseline-profile/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
BUILD_FILE = (ROOT / 'baseline-profile/build.gradle.kts').read_text(encoding='utf-8')


class BaselineProfileManifestContractTest(unittest.TestCase):
    def test_min_sdk_is_owned_by_gradle_not_manifest(self):
        self.assertNotIn('<uses-sdk', MANIFEST)
        self.assertIn('minSdk = 26', BUILD_FILE)


if __name__ == '__main__':
    unittest.main()
