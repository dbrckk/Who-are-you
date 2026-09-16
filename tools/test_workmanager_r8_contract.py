from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
APP_GRADLE = ROOT / "app" / "build.gradle.kts"
PROGUARD = ROOT / "app" / "proguard-rules.pro"


class WorkManagerR8ContractTest(unittest.TestCase):
    def test_release_graph_pins_workmanager_with_full_mode_safe_rules(self):
        gradle = APP_GRADLE.read_text(encoding="utf-8")

        self.assertIn('implementation("androidx.work:work-runtime:2.11.2")', gradle)
        self.assertIn("because(", gradle)
        self.assertIn("R8", gradle)

    def test_fix_does_not_disable_minification_or_keep_all_workmanager(self):
        gradle = APP_GRADLE.read_text(encoding="utf-8")
        proguard = PROGUARD.read_text(encoding="utf-8")

        self.assertIn("isMinifyEnabled = true", gradle)
        self.assertNotIn("-keep class androidx.work.** { *; }", proguard)


if __name__ == "__main__":
    unittest.main()
