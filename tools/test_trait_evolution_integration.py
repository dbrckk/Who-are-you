from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class TraitEvolutionIntegrationContractTest(unittest.TestCase):
    def setUp(self):
        self.store = (ROOT / 'app/src/main/java/com/whoareyou/app/ProfileStore.kt').read_text(encoding='utf-8')
        self.history = (ROOT / 'app/src/main/java/com/whoareyou/app/ScoreHistory.kt').read_text(encoding='utf-8')
        self.global_profile = (ROOT / 'app/src/main/java/com/whoareyou/app/GlobalProfile.kt').read_text(encoding='utf-8')
        self.main = (ROOT / 'app/src/main/java/com/whoareyou/app/MainActivity.kt').read_text(encoding='utf-8')
        self.profile = (ROOT / 'app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt').read_text(encoding='utf-8')
        self.evolution = (ROOT / 'app/src/main/java/com/whoareyou/app/TraitEvolution.kt').read_text(encoding='utf-8')

    def test_bounded_history_is_persisted(self):
        self.assertIn('scoreHistory: Map<String, List<Int>>', self.store)
        self.assertIn('score_history_v2', self.store)
        self.assertIn('MAX_SCORES_PER_QUIZ = 8', self.history)

    def test_global_profile_builds_temporal_trait_summary(self):
        self.assertIn('val traitEvolution: TraitEvolutionSummary', self.global_profile)
        self.assertIn('TraitEvolutionEngine.build(', self.global_profile)
        self.assertIn('storedProfile.scoreHistory', self.main)

    def test_ui_separates_new_evidence_from_real_movement(self):
        self.assertIn('TraitEvolutionCard(', self.profile)
        self.assertIn('evolution = summary.traitEvolution', self.profile)
        self.assertIn('catalog = catalog', self.profile)
        self.assertIn('NEW_EVIDENCE', self.evolution)
        self.assertIn('MOVED', self.evolution)
        self.assertIn('LOW_CONFIDENCE', self.evolution)
        self.assertIn('CONTRADICTORY', self.evolution)

    def test_legacy_scores_seed_history(self):
        self.assertIn('migratedSeries', self.history)
        self.assertIn('previous[quizId]', self.history)
        self.assertIn('existingLatest', self.history)

if __name__ == '__main__':
    unittest.main()
