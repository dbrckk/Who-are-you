import pathlib
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"


class CoreAccessibilityContractTest(unittest.TestCase):
    def read(self, filename: str) -> str:
        return (APP / filename).read_text(encoding="utf-8")

    def test_back_action_has_minimum_touch_target(self):
        source = self.read("AccessibilityUi.kt")
        self.assertIn("heightIn(min = 48.dp)", source)
        self.assertIn("TextButton(", source)

    def test_quiz_is_scrollable_and_answers_are_buttons(self):
        quiz = self.read("QuizScreenUi.kt")
        pressable = self.read("V2InteractiveUi.kt")
        self.assertIn("verticalScroll(scrollState)", quiz)
        self.assertIn("scrollState.scrollTo(0)", quiz)
        self.assertIn("V2PressableSurface(", quiz)
        self.assertIn("role: Role = Role.Button", pressable)
        self.assertIn("role = role", pressable)
        self.assertIn("AccessibleBackAction(onClick = onBack)", quiz)

    def test_shell_tabs_expose_tab_semantics(self):
        source = self.read("AppShellUi.kt")
        self.assertIn(".selectable(", source)
        self.assertIn("role = Role.Tab", source)
        self.assertIn("selected = selected", source)

    def test_result_secondary_actions_keep_touch_targets(self):
        source = self.read("ResultScreenUi.kt")
        self.assertGreaterEqual(source.count("heightIn(min = 48.dp)"), 2)

    def test_profile_progress_is_resource_backed(self):
        source = self.read("ProfileScreenUi.kt")
        self.assertIn("R.string.profile_header_progress", source)
        self.assertNotIn('dimensions • ${summary.completionPercent}', source)


if __name__ == "__main__":
    unittest.main()
