from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ReleaseCriticalProfileContractTest(unittest.TestCase):
    def setUp(self):
        self.store = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileStore.kt").read_text(encoding="utf-8")
        self.main = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
        self.recommendation = (ROOT / "app/src/main/java/com/whoareyou/app/NextQuizRecommendation.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")
        self.who_am_i = (ROOT / "app/src/main/java/com/whoareyou/app/WhoAmIPortraitUi.kt").read_text(encoding="utf-8")

    def test_profile_store_decodes_both_histories(self):
        self.assertIn("scoreHistory = ProfilePersistenceCodec.decodeScoreHistory", self.store)
        self.assertIn("timedScoreHistory = ProfilePersistenceCodec.decodeTimedScoreHistory", self.store)
        update = self.store[self.store.index("val history = ScoreHistoryEngine.update("):]
        update = update[:update.index("prefs[completedKey]")]
        self.assertEqual(update.count("scoreHistory ="), 1)
        self.assertNotIn("timedScoreHistory =", update)

    def test_profile_quiz_navigation_uses_mutable_quiz_id(self):
        profile_branch = self.main[self.main.index("AppScreen.PROFILE -> ProfileScreen("):]
        profile_branch = profile_branch[:profile_branch.index("AppScreen.QUIZ ->")]
        self.assertIn("selectedQuizId = quiz.id", profile_branch)
        self.assertIn("resetQuizAttempt()", profile_branch)
        self.assertNotIn("selectedQuiz = quiz", profile_branch)

    def test_retake_cadence_has_hard_minimum(self):
        self.assertIn("MIN_RETAKE_DAYS = 14L", self.recommendation)
        self.assertIn("if (days < MIN_RETAKE_DAYS)", self.recommendation)

    def test_profile_explains_next_quiz(self):
        self.assertIn("recommendation = summary.nextQuizRecommendation", self.profile)
        self.assertIn("NextQuizRecommendationCard(", self.who_am_i)
        self.assertIn('testTag("who_am_i_next_quiz")', self.who_am_i)
        self.assertNotIn("NextQuizRecommendationCard(", self.profile)

if __name__ == "__main__":
    unittest.main()
