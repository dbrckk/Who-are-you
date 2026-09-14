from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class TraitTimelineIntegrationContractTest(unittest.TestCase):
    def setUp(self):
        self.engine = (ROOT / "app/src/main/java/com/whoareyou/app/TraitTimeline.kt").read_text(encoding="utf-8")
        self.global_profile = (ROOT / "app/src/main/java/com/whoareyou/app/GlobalProfile.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")
        self.ui = (ROOT / "app/src/main/java/com/whoareyou/app/TraitTimelineUi.kt").read_text(encoding="utf-8")

    def test_global_profile_carries_trait_timelines(self):
        self.assertIn("val traitTimelines: List<TraitTimeline>", self.global_profile)
        self.assertIn("TraitTimelineEngine.build(", self.global_profile)

    def test_timeline_distinguishes_retake_and_new_evidence(self):
        self.assertIn("newEvidenceQuizIds: List<String>", self.engine)
        self.assertIn("retakeQuizIds: List<String>", self.engine)
        self.assertIn("changedQuizIds: List<String>", self.engine)

    def test_trait_period_comparison_tracks_score_and_confidence(self):
        self.assertIn("data class TraitTimelinePeriodComparison", self.engine)
        self.assertIn("scoreDelta: Int", self.engine)
        self.assertIn("confidenceDelta: Int", self.engine)

    def test_profile_renders_accessible_trait_history(self):
        self.assertIn("TraitTimelineCard(", self.profile)
        self.assertIn(".drawWithCache {", self.ui)
        self.assertIn("contentDescription = description", self.ui)
        self.assertIn("New evidence:", self.ui)
        self.assertIn("Retake:", self.ui)

if __name__ == "__main__":
    unittest.main()
