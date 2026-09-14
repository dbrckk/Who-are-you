from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ProfileKnowledgeMapContractTest(unittest.TestCase):
    def setUp(self):
        self.model = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileKnowledgeMap.kt").read_text(encoding="utf-8")
        self.ui = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileKnowledgeMapUi.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")

    def test_map_has_explicit_domains(self):
        for domain in ("SOCIAL", "EMOTIONAL", "THINKING", "GROWTH", "SELF_MANAGEMENT"):
            self.assertIn(domain, self.model)

    def test_map_keeps_three_knowledge_states(self):
        self.assertIn("strong: List<TraitCoverage>", self.model)
        self.assertIn("developing: List<TraitCoverage>", self.model)
        self.assertIn("unknown: List<TraitCoverage>", self.model)

    def test_profile_renders_knowledge_map(self):
        self.assertIn("ProfileKnowledgeMapCard(", self.profile)
        self.assertIn("coverage = summary.coverage", self.profile)
        self.assertIn("onTraitClick = { selectedTraitId = it }", self.profile)
        self.assertIn("TraitLocalization.label", self.ui)

if __name__ == "__main__":
    unittest.main()
