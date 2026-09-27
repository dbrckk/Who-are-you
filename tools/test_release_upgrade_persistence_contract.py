from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github/scripts/android-device-validation.sh"
SEED_TEST = ROOT / "app/src/androidTest/java/com/whoareyou/app/ReleaseUpgradeSeedTest.kt"
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

    def test_post_upgrade_verification_is_black_box_against_candidate_ui(self):
        script = SCRIPT.read_text(encoding="utf-8")
        self.assertNotIn("ReleaseUpgradeVerifyTest#verifyPersistentState", script)
        self.assertNotIn('adb install -r "$TEST_APK"\nadb shell am force-stop "$PACKAGE"\nadb logcat -c\n\nadb shell am instrument', script)
        self.assertIn('ui_has_value "device-upgrade-discover.xml" "PROFILE"', script)
        self.assertIn('scroll_until_ui_value "profile_open_habits"', script)
        self.assertIn('grep -Fq "LIFETIME UPGRADE ACTIVE"', script)
        self.assertIn('grep -Fq "4321 steps"', script)
        self.assertIn('scroll_until_ui_value "Your target: 4000"', script)

    def test_m59_retains_upgrade_probe_evidence(self):
        workflow = M59_WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("device-upgrade-state-*.txt", workflow)


if __name__ == "__main__":
    unittest.main()
