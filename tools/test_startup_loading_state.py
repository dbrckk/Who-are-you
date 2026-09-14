from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class StartupLoadingStateTest(unittest.TestCase):
    def test_profile_loading_has_visible_testable_ui(self):
        source = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
        self.assertIn('if (storedProfile == null || quizCatalog == null)', source)
        self.assertIn('BrandLoadingScreen(tag = "startup_loading")', source)
        entry = (ROOT / "app/src/main/java/com/whoareyou/app/EntryScreensUi.kt").read_text(encoding="utf-8")
        self.assertIn('.testTag(tag)', entry)
        self.assertIn('LinearProgressIndicator(', entry)

    def test_startup_does_not_return_silently_before_profile_load(self):
        source = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
        self.assertNotIn('val storedProfile = storedProfileState ?: return', source)


if __name__ == "__main__":
    unittest.main()
