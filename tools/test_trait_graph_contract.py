from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class TraitGraphContractTest(unittest.TestCase):
    def setUp(self):
        self.graph = (ROOT / "app/src/main/java/com/whoareyou/app/TraitGraph.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/GlobalProfile.kt").read_text(encoding="utf-8")
        self.ui = (ROOT / "app/src/main/java/com/whoareyou/app/TraitGraphUi.kt").read_text(encoding="utf-8")
        self.screen = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")

    def test_trait_graph_keeps_evidence_provenance(self):
        self.assertIn("data class TraitEvidence", self.graph)
        self.assertIn("quizId: String", self.graph)
        self.assertIn("contribution: Int", self.graph)
        self.assertIn("evidence: List<TraitEvidence>", self.graph)

    def test_profile_builds_graph_from_completed_dimensions(self):
        self.assertIn("val traitGraph: TraitGraph", self.profile)
        self.assertIn("TraitGraphEngine.build(dimensions)", self.profile)

    def test_profile_surfaces_fingerprint_with_non_clinical_copy(self):
        self.assertIn("TraitGraphCard(summary.traitGraph)", self.screen)
        self.assertIn("Tendencies, not diagnoses.", self.ui)
        self.assertIn("trait.evidenceCount", self.ui)

    def test_trait_scores_are_bounded(self):
        self.assertIn(".roundToInt().coerceIn(0, 100)", self.graph)

if __name__ == "__main__":
    unittest.main()
