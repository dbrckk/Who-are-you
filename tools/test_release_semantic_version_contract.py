from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
BUILD = ROOT / "app/build.gradle.kts"
ANDROID_CI = ROOT / ".github/workflows/android-ci.yml"


class ReleaseSemanticVersionContractTest(unittest.TestCase):
    def test_release_version_is_0_3_0_code_5(self):
        source = BUILD.read_text(encoding="utf-8")
        code = re.search(r"versionCode\s*=\s*(\d+)", source)
        name = re.search(r'versionName\s*=\s*"([^"]+)"', source)

        self.assertIsNotNone(code)
        self.assertIsNotNone(name)
        self.assertEqual("5", code.group(1))
        self.assertEqual("0.3.0", name.group(1))

    def test_android_ci_artifacts_include_version_code_and_sha(self):
        source = ANDROID_CI.read_text(encoding="utf-8")
        self.assertIn("Resolve app version", source)
        self.assertIn("VERSION_NAME=", source)
        self.assertIn("VERSION_CODE=", source)
        self.assertIn(
            "name: who-are-you-debug-${{ env.VERSION_NAME }}-${{ env.VERSION_CODE }}-${{ github.sha }}",
            source,
        )
        self.assertIn(
            "name: who-are-you-candidate-${{ env.VERSION_NAME }}-${{ env.VERSION_CODE }}-${{ github.sha }}",
            source,
        )


if __name__ == "__main__":
    unittest.main()
