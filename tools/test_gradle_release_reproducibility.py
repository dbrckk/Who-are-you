from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]

class GradleReleaseReproducibilityContractTest(unittest.TestCase):
    def test_all_android_workflows_pin_same_gradle_version(self):
        workflows = [
            ROOT / ".github/workflows/android-ci.yml",
            ROOT / ".github/workflows/play-candidate.yml",
            ROOT / ".github/workflows/play-internal-publish.yml",
        ]
        versions = []
        for path in workflows:
            source = path.read_text(encoding="utf-8")
            match = re.search(r"gradle-version:\s*'([^']+)'", source)
            self.assertIsNotNone(match, path.name)
            versions.append(match.group(1))
        self.assertEqual(["9.5.0", "9.5.0", "9.5.0"], versions)

    def test_build_scripts_pin_android_and_compose_plugins(self):
        root = (ROOT / "build.gradle.kts").read_text(encoding="utf-8")
        self.assertIn('id("com.android.application") version "9.3.1"', root)
        self.assertIn('id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"', root)

if __name__ == "__main__":
    unittest.main()
