from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class RuntimeMemoryLifecycleContractTest(unittest.TestCase):
    def setUp(self):
        self.billing = (ROOT / 'app/src/main/java/com/whoareyou/app/BillingManager.kt').read_text(encoding='utf-8')
        self.ads = (ROOT / 'app/src/main/java/com/whoareyou/app/AdManager.kt').read_text(encoding='utf-8')
        self.telemetry = (ROOT / 'app/src/main/java/com/whoareyou/app/Telemetry.kt').read_text(encoding='utf-8')

    def test_billing_uses_application_context_and_cancels_scope(self):
        self.assertIn('private val appContext = context.applicationContext', self.billing)
        self.assertIn('BillingClient.newBuilder(appContext)', self.billing)
        self.assertIn('supervisorJob.cancel()', self.billing)

    def test_ads_use_application_context_for_long_lived_services(self):
        self.assertIn('private val appContext = context.applicationContext', self.ads)
        self.assertIn('getConsentInformation(appContext)', self.ads)
        self.assertIn('MobileAds.initialize(appContext)', self.ads)
        self.assertIn('InterstitialAd.load(\n            appContext,', self.ads)

    def test_http_telemetry_thread_can_time_out(self):
        self.assertIn('30L,', self.telemetry)
        self.assertIn('TimeUnit.SECONDS', self.telemetry)
        self.assertIn('allowCoreThreadTimeOut(true)', self.telemetry)

if __name__ == '__main__':
    unittest.main()
