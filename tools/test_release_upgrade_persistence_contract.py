from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github/scripts/android-device-validation.sh"
SEED_TEST = ROOT / "app/src/androidTest/java/com/whoareyou/app/ReleaseUpgradeSeedTest.kt"
RAW_VERIFIER = ROOT / ".github/scripts/verify-release-upgrade-state.py"
M59_WORKFLOW = ROOT / ".github/workflows/m59-device-validation.yml"


class ReleaseUpgradePersistenceContractTest(unittest.TestCase):
    def test_upgrade_validation_seeds_before_candidate_replacement(self):
        script = SCRIPT.read_text(encoding="utf-8")
        self.assertTrue(SEED_TEST.is_file())
        self.assertIn("ReleaseUpgradeSeedTest#seedPersistentState", script)
        self.assertIn('adb install -r "$CANDIDATE_APK"', script)
        self.assertLess(
            script.index("ReleaseUpgradeSeedTest#seedPersistentState"),
            script.index('adb install -r "$CANDIDATE_APK"'),
        )

    def test_post_upgrade_verification_reads_real_candidate_datastore(self):
        script = SCRIPT.read_text(encoding="utf-8")
        self.assertTrue(RAW_VERIFIER.is_file())
        self.assertNotIn("ReleaseUpgradeVerifyTest#verifyPersistentState", script)
        self.assertNotIn("uiautomator dump", script)
        self.assertIn("adb root", script)
        self.assertIn("who_are_you_profile.preferences_pb", script)
        self.assertIn("who_are_you_behavior.preferences_pb", script)
        self.assertIn("who_are_you_behavior_goals.preferences_pb", script)
        self.assertIn("verify-release-upgrade-state.py", script)
        self.assertIn('grep -F "profile=onboarded"', script)
        self.assertIn('grep -F "behavior_steps=4321"', script)
        self.assertIn('grep -F "goal_target=4000"', script)

    def test_m59_retains_upgrade_probe_evidence(self):
        workflow = M59_WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("device-upgrade-state-*.txt", workflow)


if __name__ == "__main__":
    unittest.main()
