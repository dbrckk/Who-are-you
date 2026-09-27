from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github/scripts/android-device-validation.sh"
SEED_TEST = ROOT / "app/src/androidTest/java/com/whoareyou/app/ReleaseUpgradeSeedTest.kt"
CANDIDATE_MANIFEST = ROOT / "app/src/candidate/AndroidManifest.xml"
CANDIDATE_PROBE = ROOT / "app/src/candidate/java/com/whoareyou/app/CandidateUpgradeStateProbeReceiver.kt"
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

    def test_post_upgrade_verification_uses_candidate_only_probe(self):
        script = SCRIPT.read_text(encoding="utf-8")
        manifest = CANDIDATE_MANIFEST.read_text(encoding="utf-8")
        probe = CANDIDATE_PROBE.read_text(encoding="utf-8")

        self.assertTrue(CANDIDATE_MANIFEST.is_file())
        self.assertTrue(CANDIDATE_PROBE.is_file())
        self.assertNotIn("adb root", script)
        self.assertNotIn("verify-release-upgrade-state.py", script)
        self.assertIn("CandidateUpgradeStateProbeReceiver", manifest)
        self.assertIn("CANDIDATE_UPGRADE_STATE", manifest)
        self.assertIn("ProfileStore.observe(context).first()", probe)
        self.assertIn("BehaviorRepository.observe(context).first()", probe)
        self.assertIn("BehaviorGoalRepository.observe(context).first()", probe)
        self.assertIn("CandidateUpgradeStateProbeReceiver", script)
        self.assertIn('grep -F "onboarding=true"', script)
        self.assertIn('grep -F "ads_removed=true"', script)
        self.assertIn('grep -F "activity_state=AVAILABLE"', script)
        self.assertIn('grep -F "steps=4321"', script)
        self.assertIn('grep -F "goal_id=upgrade-probe-goal"', script)
        self.assertIn('grep -F "goal_metric=STEPS_AT_LEAST"', script)
        self.assertIn('grep -F "goal_target=4000"', script)

    def test_m59_retains_upgrade_probe_evidence(self):
        workflow = M59_WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("device-upgrade-state-*.txt", workflow)


if __name__ == "__main__":
    unittest.main()
