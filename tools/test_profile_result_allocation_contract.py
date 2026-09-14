from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ProfileResultAllocationContractTest(unittest.TestCase):
    def setUp(self):
        self.profile = (ROOT / 'app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt').read_text(encoding='utf-8')
        self.result = (ROOT / 'app/src/main/java/com/whoareyou/app/ResultScreenUi.kt').read_text(encoding='utf-8')
        self.next_ui = (ROOT / 'app/src/main/java/com/whoareyou/app/ResultNextExplorationUi.kt').read_text(encoding='utf-8')
        self.aura = (ROOT / 'app/src/main/java/com/whoareyou/app/IdentityAuraUi.kt').read_text(encoding='utf-8')

    def test_profile_sort_and_catalog_index_are_remembered(self):
        self.assertIn('val catalogById = remember(catalog)', self.profile)
        self.assertIn('val sortedDimensions = remember(summary.dimensions)', self.profile)
        self.assertIn('items(sortedDimensions', self.profile)
        self.assertNotIn('items(summary.dimensions.sortedByDescending', self.profile)
        self.assertIn('val dimensionBrush = remember(accent, companion)', self.profile)

    def test_profile_and_result_background_gradients_are_remembered(self):
        self.assertIn('val profileBackground = remember(primaryAccent, companionAccent)', self.profile)
        self.assertIn('val resultBackground = remember(accent, secondaryAccent)', self.result)
        self.assertIn('val scoreCardBrush = remember(accent, secondaryAccent)', self.result)

    def test_result_recommendation_uses_indexed_catalog_and_stable_brush(self):
        self.assertIn('val catalogById = remember(catalog)', self.next_ui)
        self.assertIn('val orderedQuizIds = remember(catalog)', self.next_ui)
        self.assertIn('catalogById[recommendation.quizId]', self.next_ui)
        self.assertIn('val cardBrush = remember(accent, companion)', self.next_ui)

    def test_identity_aura_uses_draw_cache_for_geometry(self):
        self.assertIn('.drawWithCache {', self.aura)
        self.assertIn('val orbitPoints = List(10)', self.aura)
        self.assertIn('val innerBrush = Brush.radialGradient', self.aura)

if __name__ == '__main__':
    unittest.main()
