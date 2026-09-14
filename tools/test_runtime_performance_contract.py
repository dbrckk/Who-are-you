from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class RuntimePerformanceContractTest(unittest.TestCase):
    def setUp(self):
        self.main = (ROOT / 'app/src/main/java/com/whoareyou/app/MainActivity.kt').read_text(encoding='utf-8')
        self.billing = (ROOT / 'app/src/main/java/com/whoareyou/app/BillingManager.kt').read_text(encoding='utf-8')
        self.billing_policy = (ROOT / 'app/src/main/java/com/whoareyou/app/BillingReconnectPolicy.kt').read_text(encoding='utf-8')
        self.profile = (ROOT / 'app/src/main/java/com/whoareyou/app/ProfileStore.kt').read_text(encoding='utf-8')
        self.quiz_visual = (ROOT / 'app/src/main/java/com/whoareyou/app/QuizVisualUi.kt').read_text(encoding='utf-8')
        self.onboarding = (ROOT / 'app/src/main/java/com/whoareyou/app/EntryScreensUi.kt').read_text(encoding='utf-8')
        self.discover = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt').read_text(encoding='utf-8')

    def test_catalog_parsing_is_off_main_thread(self):
        self.assertIn('produceState<List<Quiz>?>(initialValue = null, context)', self.main)
        self.assertIn('withContext(Dispatchers.IO)', self.main)
        self.assertIn('QuizRepository.load(context.applicationContext)', self.main)

    def test_external_sdks_are_after_onboarding_and_catalog_guards(self):
        manager_index = self.main.index('val billingManager = remember(context)')
        onboarding_index = self.main.index('if (!storedProfile.onboardingComplete)')
        catalog_index = self.main.index('if (!AppNavigation.hasUsableCatalog')
        self.assertGreater(manager_index, onboarding_index)
        self.assertGreater(manager_index, catalog_index)
        self.assertIn("withFrameNanos { }", self.main)

    def test_billing_reconnects_are_bounded(self):
        self.assertIn('const val maxAttempts = 5', self.billing_policy)
        self.assertIn('reconnectAttempt >= BillingReconnectPolicy.maxAttempts', self.billing)

    def test_datastore_fallback_only_handles_io_errors(self):
        self.assertIn('if (error is IOException)', self.profile)
        self.assertIn('throw error', self.profile)

    def test_reduced_motion_removes_ambient_frame_loops(self):
        for source in (self.quiz_visual, self.onboarding):
            self.assertIn('if (reduceMotion)', source)
        self.assertNotIn('rememberInfiniteTransition', self.discover)
        self.assertNotIn('infiniteRepeatable', self.discover)
        self.assertIn('glowScale?.value ?: 1f', self.quiz_visual)

    def test_app_reports_first_useful_draw(self):
        self.assertIn('reportFullyDrawn()', self.main)

if __name__ == '__main__':
    unittest.main()
