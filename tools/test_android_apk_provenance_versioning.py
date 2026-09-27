from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/android-apk.yml"


class AndroidApkProvenanceVersioningTest(unittest.TestCase):
    def test_android_apk_artifact_uses_current_gradle_version(self):
        source = WORKFLOW.read_text(encoding="utf-8")

        self.assertIn('VERSION_NAME="$(sed -n', source)
        self.assertIn('VERSION_CODE="$(sed -n', source)
        self.assertIn('who-are-you-${VERSION_NAME}-${VERSION_CODE}-debug.apk', source)
        self.assertIn('version=${VERSION_NAME}-debug', source)
        self.assertNotIn("who-are-you-0.1.0-debug.apk", source)
        self.assertNotIn("version=0.1.0-debug", source)


if __name__ == "__main__":
    unittest.main()
