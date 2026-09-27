from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/android-ci.yml"
UPGRADE_VERIFY = ROOT / "app/src/androidTest/java/com/whoareyou/app/ReleaseUpgradeVerifyTest.kt"


class ReleaseCiContractTest(unittest.TestCase):
    def test_upgrade_verification_decodes_persisted_state_through_repositories(self):
        source = UPGRADE_VERIFY.read_text(encoding="utf-8")

        self.assertIn("PreferenceDataStoreFactory.create", source)
        self.assertIn('booleanPreferencesKey("onboarding_complete")', source)
        self.assertIn('stringPreferencesKey("daily_behavior_v1")', source)
        self.assertIn('stringPreferencesKey("goals_v1")', source)
        self.assertNotIn("dataStoreBytes(", source)

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
