from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class RestoredQuizRecoveryTest(unittest.TestCase):
    def setUp(self):
        self.source = (
            ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt"
        ).read_text(encoding="utf-8")

    def test_missing_restored_quiz_does_not_fall_back_with_stale_progress(self):
        self.assertIn(
            "val selectedQuiz = quizCatalog.firstOrNull { it.id == selectedQuizId }",
            self.source,
        )
        self.assertNotIn(
            "val selectedQuiz = quizCatalog.firstOrNull { it.id == selectedQuizId } ?: quizCatalog.first()",
            self.source,
        )

    def test_missing_quiz_resets_entire_attempt_before_returning_to_discover(self):
        for expected in (
            "quizQuestionIndex = 0",
            "quizRawScore = 0",
            "pendingFinalScore = null",
            "finalScore = 0",
            "previousScoreForAttempt = null",
            "screenName = AppScreen.DISCOVER.name",
            'BrandLoadingScreen(tag = "quiz_session_recovering")',
        ):
            self.assertIn(expected, self.source)


if __name__ == "__main__":
    unittest.main()
