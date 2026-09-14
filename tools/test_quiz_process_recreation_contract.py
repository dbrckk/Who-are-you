from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class QuizProcessRecreationContractTest(unittest.TestCase):
    def test_critical_attempt_state_is_saveable(self):
        source = (
            ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt"
        ).read_text(encoding="utf-8")
        expected = (
            "var screenName by rememberSaveable",
            "var selectedQuizId by rememberSaveable",
            "var quizAttemptId by rememberSaveable",
            "var quizQuestionIndex by rememberSaveable",
            "var quizRawScore by rememberSaveable",
            "var pendingFinalScore by rememberSaveable",
            "var commitFailed by rememberSaveable",
            "var finalScore by rememberSaveable",
            "var previousScoreForAttempt by rememberSaveable",
        )
        for item in expected:
            self.assertIn(item, source)

    def test_pending_result_replays_through_idempotent_commit_effect(self):
        source = (
            ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt"
        ).read_text(encoding="utf-8")
        self.assertIn(
            "screen, selectedQuiz, quizAttemptId, pendingFinalScore",
            source,
        )


if __name__ == "__main__":
    unittest.main()
