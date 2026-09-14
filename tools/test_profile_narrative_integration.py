from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ProfileNarrativeIntegrationContractTest(unittest.TestCase):
    def setUp(self):
        self.engine = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileNarrative.kt").read_text(encoding="utf-8")
        self.ui = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileNarrativeUi.kt").read_text(encoding="utf-8")
        self.global_profile = (ROOT / "app/src/main/java/com/whoareyou/app/GlobalProfile.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")

    def test_global_profile_contains_narrative(self):
        self.assertIn("val narrative: ProfileNarrativeSummary", self.global_profile)
        self.assertIn("ProfileNarrativeEngine.build(", self.global_profile)

    def test_narrative_separates_change_from_more_evidence(self):
        self.assertIn("REPEATED_MOVEMENT", self.engine)
        self.assertIn("STABLE_WITH_MORE_EVIDENCE", self.engine)
        self.assertIn("BETTER_DOCUMENTED", self.engine)

    def test_uncertainty_blocks_overconfident_directional_copy(self):
        self.assertIn("CONTRADICTORY", self.engine)
        self.assertIn("LOW_CONFIDENCE", self.engine)
        self.assertIn("VOLATILE", self.engine)
        self.assertIn("instead of drawing a conclusion", self.ui)

    def test_profile_surfaces_factual_summary(self):
        self.assertIn("ProfileNarrativeCard(summary.narrative)", self.profile)
        self.assertIn("without diagnosis", self.ui)
        self.assertIn("sans diagnostic", self.ui)

if __name__ == "__main__":
    unittest.main()
