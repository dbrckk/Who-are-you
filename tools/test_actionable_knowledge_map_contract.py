from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ActionableKnowledgeMapContractTest(unittest.TestCase):
    def setUp(self):
        self.map_ui = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileKnowledgeMapUi.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")
        self.dialog = (ROOT / "app/src/main/java/com/whoareyou/app/TraitExplorationUi.kt").read_text(encoding="utf-8")
        self.main = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")

    def test_traits_are_clickable(self):
        self.assertIn("onTraitClick: (String) -> Unit", self.map_ui)
        self.assertIn("clickable { onTraitClick(trait.traitId) }", self.map_ui)

    def test_profile_opens_exploration_dialog(self):
        self.assertIn("TraitExplorationEngine.build(", self.profile)
        self.assertIn("TraitExplorationDialog(", self.profile)

    def test_dialog_exposes_evidence_and_next_measurement(self):
        self.assertIn("exploration.evidence", self.dialog)
        self.assertIn("recommendedQuizId", self.dialog)
        self.assertIn("onStartQuiz(quiz)", self.dialog)

    def test_profile_can_navigate_directly_to_quiz(self):
        # All quiz entry points share one handler, including the profile.
        self.assertIn("onQuizSelected = ::startQuiz", self.main)
        handler = self.main.split("fun startQuiz(quiz: Quiz)", 1)[1].split("QuizResultCommitEffect(", 1)[0]
        self.assertIn("selectedQuizId = quiz.id", handler)
        self.assertIn("navigate(AppScreen.QUIZ)", handler)

if __name__ == "__main__":
    unittest.main()
