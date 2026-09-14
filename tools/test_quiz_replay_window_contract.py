from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class QuizReplayWindowContractTest(unittest.TestCase):
    def test_replay_history_is_large_but_bounded(self):
        source = (
            ROOT / "app/src/main/java/com/whoareyou/app/ProfileStore.kt"
        ).read_text(encoding="utf-8")
        self.assertIn("MAX_COMMITTED_QUIZ_ATTEMPTS = 256", source)
        self.assertIn(".takeLast(MAX_COMMITTED_QUIZ_ATTEMPTS)", source)
        self.assertNotIn("MAX_COMMITTED_QUIZ_ATTEMPTS = 64", source)

    def test_last_attempt_per_quiz_is_still_retained_for_migration_and_replay_protection(self):
        source = (
            ROOT / "app/src/main/java/com/whoareyou/app/ProfileStore.kt"
        ).read_text(encoding="utf-8")
        self.assertIn('stringPreferencesKey("last_quiz_attempt_ids")', source)
        self.assertIn("legacyAttempts + (normalizedQuizId to normalizedAttemptId)", source)


if __name__ == "__main__":
    unittest.main()
