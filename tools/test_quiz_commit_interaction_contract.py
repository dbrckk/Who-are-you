from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class QuizCommitInteractionContractTest(unittest.TestCase):
    def setUp(self):
        self.quiz = (ROOT / "app/src/main/java/com/whoareyou/app/QuizScreenUi.kt").read_text(encoding="utf-8")
        self.interactive = (ROOT / "app/src/main/java/com/whoareyou/app/V2InteractiveUi.kt").read_text(encoding="utf-8")

    def test_system_back_is_blocked_while_result_is_saving(self):
        self.assertIn("BackHandler(enabled = isFinishing)", self.quiz)

    def test_visible_back_action_does_not_leave_during_commit(self):
        self.assertIn("val canNavigateBack = !isFinishing", self.quiz)
        self.assertIn("if (canNavigateBack) onBack()", self.quiz)

    def test_answers_are_semantically_disabled_during_commit(self):
        self.assertIn("enabled = !isFinishing", self.quiz)
        self.assertIn("enabled: Boolean = true", self.interactive)
        self.assertIn("enabled = enabled", self.interactive)


if __name__ == "__main__":
    unittest.main()
