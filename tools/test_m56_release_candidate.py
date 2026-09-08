from pathlib import Path
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

    def test_circleci_builds_and_verifies_installable_apk(self):
        config = (ROOT / ".circleci/config.yml").read_text(encoding="utf-8")
        for expected in (
            "gradle :app:testDebugUnitTest",
            "gradle :app:assembleDebugAndroidTest",
            "gradle :app:lintDebug",
            "gradle :app:lintRelease",
            "gradle :app:assembleDebug",
            "gradle :app:bundleRelease",
            "zipalign -c -v 4",
            "apksigner verify --verbose --print-certs",
            "aapt dump badging",
            "who-are-you-0.1.0-debug.apk",
            "release-candidate.json",
        ):
            self.assertIn(expected, config)

    def test_candidate_is_explicitly_non_production(self):
        config = (ROOT / ".circleci/config.yml").read_text(encoding="utf-8")
        self.assertIn("Google test IDs", config)
        self.assertIn("direct device acceptance testing; not a Play production artifact", config)


if __name__ == "__main__":
    unittest.main()
