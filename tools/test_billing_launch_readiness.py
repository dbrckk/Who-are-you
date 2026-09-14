from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class BillingLaunchReadinessTest(unittest.TestCase):
    def setUp(self):
        self.state = (ROOT / "app/src/main/java/com/whoareyou/app/BillingPriceState.kt").read_text(encoding="utf-8")
        self.manager = (ROOT / "app/src/main/java/com/whoareyou/app/BillingManager.kt").read_text(encoding="utf-8")
        self.discover = (ROOT / "app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt").read_text(encoding="utf-8")
        self.main = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")

    def test_no_hard_coded_purchase_price_remains(self):
        self.assertNotIn('€1.99', self.state)
        self.assertIn("BillingPriceLoadState", self.state)

    def test_purchase_button_requires_live_play_price(self):
        self.assertIn("enabled = premiumPrice != null", self.discover)
        self.assertIn("BillingPriceLoadState.UNAVAILABLE", self.discover)

    def test_transient_product_query_failures_retry_boundedly(self):
        self.assertIn("productQueryRetryAttempt >= 3", self.manager)
        self.assertIn("scheduleProductQueryRetry()", self.manager)
        self.assertIn("productQueryRetryJob?.cancel()", self.manager)

    def test_new_manager_resets_price_to_loading(self):
        self.assertIn("BillingPriceState.markLoading()", self.main)


if __name__ == "__main__":
    unittest.main()
