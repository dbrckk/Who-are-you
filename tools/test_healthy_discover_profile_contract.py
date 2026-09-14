from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class HealthyDiscoverProfileContractTest(unittest.TestCase):
    def setUp(self):
        self.discover = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt').read_text(encoding='utf-8')
        self.profile = (ROOT / 'app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt').read_text(encoding='utf-8')
        self.en = (ROOT / 'app/src/main/res/values/strings.xml').read_text(encoding='utf-8')
        self.fr = (ROOT / 'app/src/main/res/values-fr/strings.xml').read_text(encoding='utf-8')

    def test_discover_exploration_precedes_retention_metrics(self):
        self.assertLess(self.discover.index('GuidedJourneySection('), self.discover.index('DiscoverMomentumCard('))
        self.assertLess(self.discover.index('DiscoverLibrary('), self.discover.index('RetentionEngagementSection('))

    def test_profile_social_actions_are_optional_and_not_accent_primary(self):
        self.assertIn('R.string.profile_optional_social_actions', self.profile)
        self.assertIn('enabled = summary.dimensions.isNotEmpty()', self.profile)
        share = self.profile.index('AppEvents.profileShare')
        nearby = self.profile[share:share + 900]
        self.assertIn('containerColor = V2Colors.SurfaceElevated', nearby)

    def test_optional_profile_copy_is_localized(self):
        self.assertIn('your profile stays useful without sharing it', self.en)
        self.assertIn('ton profil reste utile sans avoir besoin de le partager', self.fr)

if __name__ == '__main__':
    unittest.main()
