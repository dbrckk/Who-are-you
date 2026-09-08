import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"


class M50AnalyticsTest(unittest.TestCase):
    def test_core_funnel_is_explicit(self):
        source = (APP / "AppEvents.kt").read_text(encoding="utf-8")
        for event in (
            '"app_open"',
            '"onboarding_view"',
            '"onboarding_complete"',
            '"screen_view"',
            '"test_start"',
            '"test_abandon"',
            '"test_complete"',
            '"result_view"',
            '"premium_view"',
            '"purchase_start"',
            '"purchase_cancel"',
            '"purchase_success"',
        ):
            self.assertIn(event, source)

    def test_behavioral_scores_are_bucketed(self):
        source = (APP / "AppEvents.kt").read_text(encoding="utf-8")
        self.assertIn("score_bucket", source)
        self.assertIn("compatibility_bucket", source)
        self.assertIn("confidence_bucket", source)
        self.assertNotIn('"score" to score', source)
        self.assertNotIn('"compatibility" to compatibility', source)

    def test_remote_errors_do_not_send_message_or_stack(self):
        source = (APP / "Telemetry.kt").read_text(encoding="utf-8")
        http_section = source.split("internal class HttpEventSink", 1)[1]
        self.assertNotIn('put("message"', http_section)
        self.assertNotIn('put("stack"', http_section)
        self.assertIn("TelemetryPrivacy.params", http_section)

    def test_main_app_wires_activation_and_abandonment(self):
        source = (APP / "MainActivity.kt").read_text(encoding="utf-8")
        for call in (
            "AppEvents.onboardingView()",
            "AppEvents.onboardingComplete()",
            "AppEvents.screenView(screen)",
            "AppEvents.testAbandon(selectedQuiz.id",
            "AppEvents.resultView(selectedQuiz.id, score)",
        ):
            self.assertIn(call, source)

    def test_billing_wires_conversion_steps(self):
        source = (APP / "BillingManager.kt").read_text(encoding="utf-8")
        self.assertIn("AppEvents.premiumView()", source)
        self.assertIn("AppEvents.purchaseStart(REMOVE_ADS_PRODUCT_ID)", source)
        self.assertIn("AppEvents.purchaseCancel(REMOVE_ADS_PRODUCT_ID)", source)
        self.assertIn("AppEvents.purchaseSuccess(REMOVE_ADS_PRODUCT_ID)", source)


if __name__ == "__main__":
    unittest.main()
