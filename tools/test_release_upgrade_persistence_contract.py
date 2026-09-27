from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github/scripts/android-device-validation.sh"
ANDROID_TEST = ROOT / "app/src/androidTest/java/com/whoareyou/app/ReleaseUpgradePersistenceTest.kt"


class ReleaseUpgradePersistenceContractTest(unittest.TestCase):
    def test_upgrade_validation_seeds_and_verifies_real_persistent_state(self):
        script = SCRIPT.read_text(encoding="utf-8")
        self.assertTrue(ANDROID_TEST.is_file())
        self.assertIn("ReleaseUpgradePersistenceTest#seedPersistentState", script)
        self.assertIn("ReleaseUpgradePersistenceTest#verifyPersistentState", script)
        self.assertIn('adb install -r "$CANDIDATE_APK"', script)
        self.assertLess(
            script.index("ReleaseUpgradePersistenceTest#seedPersistentState"),
            script.index('adb install -r "$CANDIDATE_APK"'),
        )
        self.assertGreater(
            script.index("ReleaseUpgradePersistenceTest#verifyPersistentState"),
            script.index('adb install -r "$CANDIDATE_APK"'),
        )


if __name__ == "__main__":
    unittest.main()
