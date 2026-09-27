from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github/scripts/android-device-validation.sh"
SEED_TEST = ROOT / "app/src/androidTest/java/com/whoareyou/app/ReleaseUpgradeSeedTest.kt"
VERIFY_TEST = ROOT / "app/src/androidTest/java/com/whoareyou/app/ReleaseUpgradeVerifyTest.kt"


class ReleaseUpgradePersistenceContractTest(unittest.TestCase):
    def test_upgrade_validation_seeds_and_verifies_real_persistent_state(self):
        script = SCRIPT.read_text(encoding="utf-8")
        self.assertTrue(SEED_TEST.is_file())
        self.assertTrue(VERIFY_TEST.is_file())
        self.assertIn("ReleaseUpgradeSeedTest#seedPersistentState", script)
        self.assertIn("ReleaseUpgradeVerifyTest#verifyPersistentState", script)
        self.assertIn('adb install -r "$CANDIDATE_APK"', script)
        self.assertLess(
            script.index("ReleaseUpgradeSeedTest#seedPersistentState"),
            script.index('adb install -r "$CANDIDATE_APK"'),
        )
        self.assertGreater(
            script.index("ReleaseUpgradeVerifyTest#verifyPersistentState"),
            script.index('adb install -r "$CANDIDATE_APK"'),
        )

    def test_upgrade_reinstalls_minification_safe_probe_after_app_replacement(self):
        script = SCRIPT.read_text(encoding="utf-8")
        self.assertIn('TEST_APK="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"', script)
        self.assertIn(":app:assembleDebugAndroidTest", script)
        self.assertNotIn(":app:assembleCandidateAndroidTest", script)
        candidate_install = script.index('adb install -r "$CANDIDATE_APK"')
        test_installs = [
            index
            for index in range(len(script))
            if script.startswith('adb install -r "$TEST_APK"', index)
        ]
        self.assertGreaterEqual(len(test_installs), 2)
        self.assertLess(test_installs[0], candidate_install)
        self.assertGreater(test_installs[-1], candidate_install)

    def test_post_upgrade_verifier_does_not_link_against_minified_app_symbols(self):
        verifier = VERIFY_TEST.read_text(encoding="utf-8")
        for forbidden in (
            "ProfileStore",
            "BehaviorRepository",
            "BehaviorGoalRepository",
            "BehaviorGoal",
            "DailyBehaviorAggregate",
        ):
            self.assertNotIn(forbidden, verifier)


if __name__ == "__main__":
    unittest.main()
