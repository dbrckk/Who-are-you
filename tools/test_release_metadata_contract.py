from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
BUILD = ROOT / "app/build.gradle.kts"
CI = ROOT / ".github/workflows/android-ci.yml"


class ReleaseMetadataContractTest(unittest.TestCase):
    def test_release_version_is_0_3_0_code_5(self):
        source = BUILD.read_text(encoding="utf-8")
        self.assertRegex(source, r'\bversionCode\s*=\s*5\b')
        self.assertRegex(source, r'\bversionName\s*=\s*"0\.3\.0"')

    def test_candidate_artifact_includes_version_code_and_sha(self):
        source = CI.read_text(encoding="utf-8")
        self.assertIn("Resolve release metadata", source)
        self.assertIn("APP_VERSION_NAME", source)
        self.assertIn("APP_VERSION_CODE", source)
        self.assertIn(
            "who-are-you-candidate-${{ env.APP_VERSION_NAME }}-${{ env.APP_VERSION_CODE }}-${{ github.sha }}",
            source,
        )


if __name__ == "__main__":
    unittest.main()
