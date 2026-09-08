import pathlib
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
APP_PACKAGE = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"
MAIN_ACTIVITY = APP_PACKAGE / "MainActivity.kt"


class MainActivityArchitectureTest(unittest.TestCase):
    def test_main_activity_stays_a_lightweight_shell(self):
        source = MAIN_ACTIVITY.read_text(encoding="utf-8")

        self.assertLess(
            len(source.encode("utf-8")),
            16_000,
            "MainActivity.kt should remain a lightweight app shell, not become an all-in-one UI file again.",
        )

        for legacy_symbol in (
            "fun DiscoverScreen(",
            "fun GlobalProfileScreen(",
            "fun QuizCard(",
            "private fun QuizScreen(",
            "private fun ResultScreen(",
        ):
            self.assertNotIn(legacy_symbol, source)

    def test_shell_routes_to_extracted_screens(self):
        source = MAIN_ACTIVITY.read_text(encoding="utf-8")
        for call in ("DiscoverHub(", "ProfileScreen(", "QuizScreen(", "ResultScreen("):
            self.assertIn(call, source)

    def test_extracted_screen_files_exist(self):
        for filename in (
            "DiscoverHubUi.kt",
            "ProfileScreenUi.kt",
            "QuizScreenUi.kt",
            "ResultScreenUi.kt",
        ):
            self.assertTrue((APP_PACKAGE / filename).is_file(), f"Missing extracted screen: {filename}")


if __name__ == "__main__":
    unittest.main()
