from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class M51ReleaseReadinessTest(unittest.TestCase):
    def test_release_build_is_optimized(self):
        gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        self.assertIn("isMinifyEnabled = true", gradle)
        self.assertIn("isShrinkResources = true", gradle)
        self.assertIn('getDefaultProguardFile("proguard-android-optimize.txt")', gradle)
        self.assertIn('create("playRelease")', gradle)
        self.assertIn("playReleaseRequested", gradle)

    def test_signing_material_is_never_committed(self):
        gitignore = (ROOT / ".gitignore").read_text(encoding="utf-8")
        for pattern in ("*.jks", "*.keystore", "*.p12", "*.pfx"):
            self.assertIn(pattern, gitignore)

    def test_manifest_uses_production_safe_defaults(self):
        manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        self.assertIn('android:allowBackup="false"', manifest)
        self.assertIn('android:usesCleartextTraffic="false"', manifest)
        self.assertIn('android:exported="false"', manifest)
        self.assertIn('android:autoVerify="true"', manifest)

    def test_github_actions_proves_release_variant(self):
        config = (ROOT / ".github/workflows/m56-main-rc.yml").read_text(encoding="utf-8")
        self.assertIn("gradle :app:lintRelease --stacktrace", config)
        self.assertIn("gradle :app:bundleRelease --stacktrace", config)
        self.assertIn("release-unsigned.aab", config)
        self.assertIn("sha256sum", config)

    def test_protected_play_workflows_exist(self):
        candidate = (ROOT / ".github/workflows/play-candidate.yml").read_text(encoding="utf-8")
        publish = (ROOT / ".github/workflows/play-internal-publish.yml").read_text(encoding="utf-8")
        self.assertIn("bundlePlayRelease", candidate)
        self.assertIn("jarsigner -verify", candidate)
        self.assertIn("environment: play-internal", candidate)
        self.assertIn("publish_to_play", publish)
        self.assertIn("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_B64", publish)
        self.assertIn("--commit", publish)


if __name__ == "__main__":
    unittest.main()
