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

    def test_upgrade_uses_variant_aligned_instrumentation_apks(self):
        script = SCRIPT.read_text(encoding="utf-8")
        self.assertIn('DEBUG_TEST_APK="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"', script)
        self.assertIn(
            'CANDIDATE_TEST_APK="app/build/outputs/apk/androidTest/candidate/app-candidate-androidTest.apk"',
            script,
        )
        self.assertIn(":app:assembleDebugAndroidTest", script)
        self.assertIn(":app:assembleCandidateAndroidTest", script)
        self.assertLess(
            script.index('adb install -r "$DEBUG_TEST_APK"'),
            script.index('adb install -r "$CANDIDATE_APK"'),
        )
        self.assertGreater(
            script.index('adb install -r "$CANDIDATE_TEST_APK"'),
            script.index('adb install -r "$CANDIDATE_APK"'),
        )

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
