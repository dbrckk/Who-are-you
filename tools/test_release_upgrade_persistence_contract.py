from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github/scripts/android-device-validation.sh"
SEED_TEST = ROOT / "app/src/androidTest/java/com/whoareyou/app/ReleaseUpgradeSeedTest.kt"
CANDIDATE_MANIFEST = ROOT / "app/src/candidate/AndroidManifest.xml"
CANDIDATE_PROBE = ROOT / "app/src/candidate/java/com/whoareyou/app/CandidateUpgradeStateProbeProvider.kt"
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

    def test_post_upgrade_verification_uses_candidate_only_provider(self):
        script = SCRIPT.read_text(encoding="utf-8")
        manifest = CANDIDATE_MANIFEST.read_text(encoding="utf-8")
        probe = CANDIDATE_PROBE.read_text(encoding="utf-8")

        self.assertTrue(CANDIDATE_MANIFEST.is_file())
        self.assertTrue(CANDIDATE_PROBE.is_file())
        self.assertNotIn("adb root", script)
        self.assertNotIn("am broadcast", script)
        self.assertIn("CandidateUpgradeStateProbeProvider", manifest)
        self.assertIn("candidate-upgrade-probe", manifest)
        self.assertIn("ProfileStore.observe(appContext).first()", probe)
        self.assertIn("BehaviorRepository.observe(appContext).first()", probe)
        self.assertIn("BehaviorGoalRepository.observe(appContext).first()", probe)
        self.assertIn("withTimeout(5_000)", probe)
        self.assertIn("adb shell content call", script)
        probe_call = script.index("adb shell content call")
        start = script.index('UPGRADE_CANDIDATE_START="$(adb shell am start -W -n "$ACTIVITY")"')
        self.assertLess(probe_call, start)
        self.assertIn('content://$PACKAGE.candidate-upgrade-probe', script)
        self.assertIn("--method state", script)
        self.assertIn('grep -F "onboarding=true"', script)
        self.assertIn('grep -F "ads_removed=true"', script)
        self.assertIn('grep -F "activity_state=AVAILABLE"', script)
        self.assertIn('grep -F "steps=4321"', script)
        self.assertIn('grep -F "goal_id=upgrade-probe-goal"', script)
        self.assertIn('grep -F "goal_metric=STEPS_AT_LEAST"', script)
        self.assertIn('grep -F "goal_target=4000"', script)

    def test_post_upgrade_runtime_checks_do_not_reenter_ui_automation(self):
        script = SCRIPT.read_text(encoding="utf-8")
        candidate_replace = script.index('adb install -r "$CANDIDATE_APK"', script.index("ReleaseUpgradeSeedTest#seedPersistentState"))
        post_upgrade = script[candidate_replace:]
        self.assertIn("validate_running_app_no_ui upgrade-candidate", post_upgrade)
        self.assertNotIn("validate_running_app_no_ui upgrade-relaunch", post_upgrade)
        no_ui = script[script.index("validate_running_app_no_ui() {"):script.index("smoke_apk() {")]
        self.assertNotIn("dumpsys activity exit-info", no_ui)
        self.assertNotIn("adb logcat", no_ui)
        self.assertIn('adb shell pidof "$PACKAGE"', no_ui)
        self.assertNotIn('capture_visual_evidence "upgrade-candidate"', post_upgrade)
        self.assertNotIn('capture_visual_evidence "upgrade-relaunch"', post_upgrade)
        self.assertNotIn('"upgrade-candidate"\n    "upgrade-relaunch"', script)
        self.assertLess(
            script.index('capture_accessibility_variant "candidate-font-130"'),
            script.index("ReleaseUpgradeSeedTest#seedPersistentState"),
        )

    def test_m59_retains_upgrade_probe_evidence(self):
        workflow = M59_WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("device-upgrade-state-*.txt", workflow)


if __name__ == "__main__":
    unittest.main()
