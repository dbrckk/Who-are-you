from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/android-ci.yml"


class ReleaseCiContractTest(unittest.TestCase):
    def test_ci_builds_release_like_candidate_apk(self):
        source = WORKFLOW.read_text(encoding="utf-8")

        self.assertIn("Build candidate APK", source)
        self.assertIn("gradle :app:assembleCandidate --stacktrace", source)
        self.assertIn(
            "app/build/outputs/apk/candidate/app-candidate.apk",
            source,
        )
        self.assertIn(
            "app/build/outputs/apk/candidate/app-candidate.apk.sha256",
            source,
        )
        self.assertIn("Upload candidate APK", source)


if __name__ == "__main__":
    unittest.main()
