from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class QuizResultPersistenceFeedbackTest(unittest.TestCase):
    def setUp(self):
        self.main = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
        self.quiz = (ROOT / "app/src/main/java/com/whoareyou/app/QuizScreenUi.kt").read_text(encoding="utf-8")
        self.en = (ROOT / "app/src/main/res/values/strings.xml").read_text(encoding="utf-8")
        self.fr = (ROOT / "app/src/main/res/values-fr/strings.xml").read_text(encoding="utf-8")

    def test_saving_and_failure_states_are_visible_and_localized(self):
        for source in (self.en, self.fr):
            self.assertIn('name="quiz_result_saving"', source)
            self.assertIn('name="quiz_result_save_failed"', source)
        self.assertIn('tag = "quiz_result_saving"', self.quiz)
        self.assertIn('tag = "quiz_result_save_failed"', self.quiz)

    def test_commit_failure_preserves_retry_path(self):
        self.assertIn("var commitFailed by rememberSaveable", self.main)
        self.assertIn("pendingFinalScore = null", self.main)
        self.assertIn("commitFailed = true", self.main)
        self.assertIn("commitFailed = commitFailed", self.main)

    def test_new_finish_attempt_clears_previous_failure(self):
        self.assertIn("commitFailed = false", self.main)
        self.assertIn("pendingFinalScore = score", self.main)


if __name__ == "__main__":
    unittest.main()
