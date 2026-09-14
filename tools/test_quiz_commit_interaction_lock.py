from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class QuizCommitInteractionLockTest(unittest.TestCase):
    def test_quiz_screen_exposes_finishing_state(self):
        source = (ROOT / "app/src/main/java/com/whoareyou/app/QuizScreenUi.kt").read_text(encoding="utf-8")
        self.assertIn("isFinishing: Boolean", source)
        self.assertIn("if (isFinishing) return@V2PressableSurface", source)

    def test_main_activity_wires_commit_state_into_quiz_screen(self):
        source = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
        self.assertIn("isFinishing = quizFinishing", source)


if __name__ == "__main__":
    unittest.main()
