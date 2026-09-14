from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ProfileCoverageIntegrationContractTest(unittest.TestCase):
    def setUp(self):
        self.coverage = (ROOT / 'app/src/main/java/com/whoareyou/app/ProfileCoverage.kt').read_text(encoding='utf-8')
        self.global_profile = (ROOT / 'app/src/main/java/com/whoareyou/app/GlobalProfile.kt').read_text(encoding='utf-8')
        self.profile_ui = (ROOT / 'app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt').read_text(encoding='utf-8')
        self.result_engine = (ROOT / 'app/src/main/java/com/whoareyou/app/ResultNextExploration.kt').read_text(encoding='utf-8')
        self.discover = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverPersonalization.kt').read_text(encoding='utf-8')

    def test_global_profile_carries_coverage(self):
        self.assertIn('val coverage: ProfileCoverage', self.global_profile)
        self.assertIn('ProfileCoverageEngine.build(catalog, traitGraph)', self.global_profile)

    def test_profile_surfaces_coverage_card(self):
        self.assertIn('ProfileCoverageCard(summary.coverage)', self.profile_ui)

    def test_recommendation_engine_targets_uncertainty(self):
        self.assertIn('CoverageRecommendationEngine.recommend', self.result_engine)
        self.assertIn('ResultNextReason.PROFILE_GAP', self.result_engine)
        self.assertIn('CoverageRecommendationEngine.recommend', self.discover)
        self.assertIn('DiscoverRecommendationReason.PROFILE_GAP', self.discover)

    def test_coverage_model_tracks_confidence_and_contradictions(self):
        self.assertIn('averageConfidence: Int', self.coverage)
        self.assertIn('contradictoryEvidenceCount: Int', self.coverage)
        self.assertIn('uncertainTraitCount: Int', self.coverage)

if __name__ == '__main__':
    unittest.main()
