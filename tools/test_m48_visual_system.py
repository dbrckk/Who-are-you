import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"


class M48VisualSystemTest(unittest.TestCase):
    def test_design_system_exposes_shared_type_and_motion_tokens(self):
        source = (APP / "V2DesignSystem.kt").read_text(encoding="utf-8")
        for token in (
            "object V2Type",
            "val Hero",
            "val Question",
            "val SectionTitle",
            "val BodyStrong",
            "val Metric",
            "val Caption",
            "object V2Motion",
            "PressedScale",
        ):
            self.assertIn(token, source)

    def test_quiz_uses_shared_pressable_surface_and_typography(self):
        source = (APP / "QuizScreenUi.kt").read_text(encoding="utf-8")
        self.assertIn("V2PressableSurface(", source)
        self.assertIn("V2Type.Question", source)
        self.assertIn("V2Type.BodyStrong", source)

    def test_profile_and_result_use_shared_hero_typography(self):
        for filename in ("ProfileScreenUi.kt", "ResultScreenUi.kt"):
            source = (APP / filename).read_text(encoding="utf-8")
            self.assertIn("V2Type.Hero", source, filename)
            self.assertIn("V2Type.Eyebrow", source, filename)

    def test_press_feedback_is_animated_and_reusable(self):
        source = (APP / "V2InteractiveUi.kt").read_text(encoding="utf-8")
        self.assertIn("collectIsPressedAsState", source)
        self.assertIn("animateFloatAsState", source)
        self.assertIn("animateColorAsState", source)
        self.assertIn("fun V2PressableCard(", source)

    def test_discover_core_uses_shared_typography(self):
        for filename in (
            "DiscoverHubUi.kt",
            "DiscoverCollectionsUi.kt",
            "DiscoverLibraryUi.kt",
            "PersonalizedDiscoverUi.kt",
        ):
            source = (APP / filename).read_text(encoding="utf-8")
            self.assertIn("V2Type.", source, filename)

    def test_discover_clickable_cards_use_press_feedback(self):
        for filename in (
            "DiscoverHubUi.kt",
            "DiscoverCollectionsUi.kt",
            "DiscoverLibraryUi.kt",
            "PersonalizedDiscoverUi.kt",
        ):
            source = (APP / filename).read_text(encoding="utf-8")
            self.assertIn("V2PressableCard(", source, filename)

    def test_shell_has_animated_press_feedback(self):
        source = (APP / "AppShellUi.kt").read_text(encoding="utf-8")
        self.assertIn("collectIsPressedAsState", source)
        self.assertIn("animateFloatAsState", source)
        self.assertIn("V2Motion.PressedScale", source)
        self.assertIn("role = Role.Tab", source)


if __name__ == "__main__":
    unittest.main()
