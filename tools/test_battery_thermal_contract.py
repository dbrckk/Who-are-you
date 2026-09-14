from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class BatteryThermalContractTest(unittest.TestCase):
    def setUp(self):
        self.motion = (ROOT / 'app/src/main/java/com/whoareyou/app/V2MotionPreferences.kt').read_text(encoding='utf-8')
        self.visual = (ROOT / 'app/src/main/java/com/whoareyou/app/QuizVisualUi.kt').read_text(encoding='utf-8')
        self.ads = (ROOT / 'app/src/main/java/com/whoareyou/app/AdManager.kt').read_text(encoding='utf-8')

    def test_power_saver_disables_decorative_motion(self):
        self.assertIn('PowerManager', self.motion)
        self.assertIn('isPowerSaveMode', self.motion)
        self.assertIn('animationsDisabledBySystem(context) || powerSaveModeEnabled(context)', self.motion)

    def test_compact_quiz_artwork_is_static(self):
        self.assertIn('reducedMotionEnabled() || compact', self.visual)

    def test_discover_idle_has_no_infinite_animation(self):
        discover = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt').read_text(encoding='utf-8')
        self.assertNotIn('rememberInfiniteTransition', discover)

    def test_ads_are_not_preloaded_at_consent_startup(self):
        start_block = self.ads.split('fun start(activity: Activity?)', 1)[1].split('fun showPrivacyOptions', 1)[0]
        self.assertNotIn('MobileAds.initialize', start_block)
        self.assertNotIn('load()', start_block)

    def test_interstitial_preload_waits_until_near_frequency_threshold(self):
        self.assertIn('resultTransitionsSinceAd == RESULTS_BETWEEN_ADS - 1', self.ads)
        self.assertIn('initializeAndLoadIfNeeded()', self.ads)
        dismissed = self.ads.split('override fun onAdDismissedFullScreenContent()', 1)[1].split('}', 1)[0]
        self.assertNotIn('load()', dismissed)

if __name__ == '__main__':
    unittest.main()
