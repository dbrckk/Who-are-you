from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class PersistedResultScoreContractTest(unittest.TestCase):
    def setUp(self):
        self.effect = (
            ROOT / "app/src/main/java/com/whoareyou/app/QuizResultCommitEffect.kt"
        ).read_text(encoding="utf-8")
        self.main = (
            ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt"
        ).read_text(encoding="utf-8")

    def test_result_callback_uses_persisted_profile_score(self):
        self.assertIn("import kotlinx.coroutines.flow.first", self.effect)
        self.assertIn("ProfileStore.observe(context).first().latestScores[quiz.id]", self.effect)
        self.assertIn("onCommitted(persistedScore)", self.effect)

    def test_main_activity_sets_final_score_only_after_commit_callback(self):
        self.assertIn("onCommitted = { persistedScore ->", self.main)
        self.assertIn("finalScore = persistedScore", self.main)
        finish_block = self.main.split("onFinished = { score ->", 1)[1].split("}", 2)[0]
        self.assertNotIn("finalScore = score", finish_block)
        self.assertIn("pendingFinalScore = score", finish_block)


if __name__ == "__main__":
    unittest.main()
