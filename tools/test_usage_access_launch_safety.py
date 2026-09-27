from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
USAGE_ACCESS = ROOT / "app/src/main/java/com/whoareyou/app/UsageAccess.kt"
MAIN_ACTIVITY = ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt"


class UsageAccessLaunchSafetyTest(unittest.TestCase):
    def test_usage_access_settings_intent_is_resolved_before_launch(self):
        usage = USAGE_ACCESS.read_text(encoding="utf-8")
        main = MAIN_ACTIVITY.read_text(encoding="utf-8")

        self.assertIn("fun settingsIntent(context: Context): Intent?", usage)
        self.assertIn("resolveActivity(context.packageManager)", usage)
        self.assertIn("val settingsIntent = UsageAccess.settingsIntent(context)", main)
        self.assertIn("if (settingsIntent != null)", main)
        self.assertIn("usageAccessLauncher.launch(settingsIntent)", main)
        self.assertIn("BehaviorSourceState.UNSUPPORTED", main)
        self.assertNotIn("usageAccessLauncher.launch(UsageAccess.settingsIntent())", main)


if __name__ == "__main__":
    unittest.main()
