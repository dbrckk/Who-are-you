from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]


class M56ReleaseCandidateTest(unittest.TestCase):
    def test_manifest_has_launcher_identity(self):
        manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        self.assertIn('android:icon="@mipmap/ic_launcher"', manifest)
        self.assertIn('android:roundIcon="@mipmap/ic_launcher_round"', manifest)

    def test_adaptive_and_monochrome_icons_exist(self):
        required = (
            "app/src/main/res/drawable/ic_launcher_foreground.xml",
            "app/src/main/res/drawable/ic_launcher_monochrome.xml",
            "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
            "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml",
            "app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml",
            "app/src/main/res/mipmap-anydpi-v33/ic_launcher_round.xml",
        )
        for path in required:
            self.assertTrue((ROOT / path).is_file(), path)
        themed = (ROOT / "app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml").read_text(encoding="utf-8")
        self.assertIn("<monochrome", themed)

    def test_candidate_build_is_release_like_and_installable(self):
        gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        self.assertIn('create("candidate")', gradle)
        self.assertIn('initWith(getByName("release"))', gradle)
        self.assertIn('signingConfig = signingConfigs.getByName("debug")', gradle)
        self.assertIn("isMinifyEnabled = true", gradle)
        self.assertIn("isShrinkResources = true", gradle)

    def test_github_actions_builds_and_verifies_installable_apk(self):
        config = (ROOT / ".github/workflows/m56-main-rc.yml").read_text(encoding="utf-8")
        gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        version_name = re.search(r'versionName\s*=\s*"([^"]+)"', gradle)
        version_code = re.search(r'versionCode\s*=\s*(\d+)', gradle)
        self.assertIsNotNone(version_name)
        self.assertIsNotNone(version_code)
        version_name = version_name.group(1)
        version_code = version_code.group(1)

        for expected in (
            "release_candidate:",
            "gradle :app:testDebugUnitTest",
            "gradle :app:assembleDebugAndroidTest",
            "gradle :app:lintCandidate",
            "gradle :app:lintRelease",
            "gradle :app:assembleCandidate",
            "gradle :app:bundleRelease",
            "zipalign\" -c -v 4",
            "apksigner\" verify --verbose --print-certs",
            "aapt\" dump badging",
            f'VERSION_NAME="{version_name}"',
            f'VERSION_CODE="{version_code}"',
            f"who-are-you-{version_name}-rc",
        ):
            self.assertIn(expected, config)

    def test_circleci_config_is_removed(self):
        self.assertFalse((ROOT / ".circleci/config.yml").exists())


if __name__ == "__main__":
    unittest.main()
