import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"


class M49ArchitectureCleanupTest(unittest.TestCase):
    def test_main_activity_is_orchestration_only(self):
        source = (APP / "MainActivity.kt").read_text(encoding="utf-8")
        self.assertNotIn("private enum class Screen", source)
        self.assertNotIn("private fun OnboardingScreen", source)
        self.assertNotIn("private fun CatalogUnavailableScreen", source)
        self.assertIn("AppScreen.DISCOVER", source)
        self.assertIn("AppShellNavigation.tabFor(screen)", source)
        self.assertLess(len(source), 10000)

    def test_entry_screens_are_extracted(self):
        source = (APP / "EntryScreensUi.kt").read_text(encoding="utf-8")
        self.assertIn("fun OnboardingScreen", source)
        self.assertIn("fun CatalogUnavailableScreen", source)
        self.assertIn("V2Type.Body", source)

    def test_challenge_activity_is_deep_link_orchestration_only(self):
        source = (APP / "ChallengeActivity.kt").read_text(encoding="utf-8")
        self.assertIn("ChallengeShare.parse", source)
        self.assertIn("ChallengeFlow", source)
        self.assertIn("InvalidChallengeScreen", source)
        self.assertNotIn("@Composable", source)
        self.assertNotIn("ChallengeInk", source)
        self.assertLess(len(source), 3500)

    def test_challenge_ui_uses_shared_v2_system(self):
        source = (APP / "ChallengeUi.kt").read_text(encoding="utf-8")
        for token in (
            "V2Colors.Ink",
            "V2Type.Question",
            "V2PressableSurface",
            "verticalScroll",
            "heightIn(min = 56.dp)",
        ):
            self.assertIn(token, source)
        for legacy in ("ChallengeInk", "ChallengePanel", "ChallengeViolet", "ChallengeCyan", "ChallengeMuted"):
            self.assertNotIn(legacy, source)


if __name__ == "__main__":
    unittest.main()
