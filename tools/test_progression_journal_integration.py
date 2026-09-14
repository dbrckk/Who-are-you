from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ProgressionJournalIntegrationContractTest(unittest.TestCase):
    def setUp(self):
        self.journal = (ROOT / "app/src/main/java/com/whoareyou/app/DimensionJournal.kt").read_text(encoding="utf-8")
        self.journal_ui = (ROOT / "app/src/main/java/com/whoareyou/app/DimensionJournalUi.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")
        self.trait = (ROOT / "app/src/main/java/com/whoareyou/app/TraitEvolution.kt").read_text(encoding="utf-8")
        self.trait_ui = (ROOT / "app/src/main/java/com/whoareyou/app/TraitEvolutionUi.kt").read_text(encoding="utf-8")

    def test_journal_keeps_dates_and_period_comparison(self):
        self.assertIn("epochDay: Long", self.journal)
        self.assertIn("data class PeriodComparison", self.journal)
        self.assertIn("earlierAverage", self.journal)
        self.assertIn("recentAverage", self.journal)

    def test_profile_opens_journal_from_dimension(self):
        self.assertIn("selectedJournalTrend", self.profile)
        self.assertIn("DimensionJournalDialog(", self.profile)
        self.assertIn("profile_history_available", self.profile)

    def test_journal_ui_formats_real_dates(self):
        self.assertIn("LocalDate.ofEpochDay", self.journal_ui)
        self.assertIn("DateTimeFormatter.ofLocalizedDate", self.journal_ui)
        self.assertIn("Earlier period", self.journal_ui)
        self.assertIn("Recent period", self.journal_ui)

    def test_new_trait_evidence_keeps_source_quiz_ids(self):
        self.assertIn("newEvidenceQuizIds: List<String>", self.trait)
        self.assertIn("newEvidenceQuizIds", self.trait_ui)
        self.assertIn("New evidence via", self.trait_ui)

if __name__ == "__main__":
    unittest.main()
