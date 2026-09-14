from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class PublicLaunchExperienceContractTest(unittest.TestCase):
    def setUp(self):
        self.discover = (ROOT / "app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")
        self.entry = (ROOT / "app/src/main/java/com/whoareyou/app/EntryScreensUi.kt").read_text(encoding="utf-8")
        self.en = (ROOT / "app/src/main/res/values/strings.xml").read_text(encoding="utf-8")
        self.fr = (ROOT / "app/src/main/res/values-fr/strings.xml").read_text(encoding="utf-8")

    def test_privacy_and_support_are_permanently_accessible(self):
        self.assertIn("PrivacyAndSupportCard()", self.discover)
        self.assertIn("https://dbrckk.github.io/Who-are-you/privacy/", self.discover)
        self.assertIn("mailto:dbrak7108@gmail.com", self.discover)

    def test_profile_deletion_is_confirmed(self):
        self.assertIn("AlertDialog(", self.profile)
        self.assertIn("onResetLocalData()", self.profile)

    def test_purchase_ui_never_invents_a_price(self):
        self.assertIn("BillingPriceState.formattedPrice", self.discover)
        self.assertIn("enabled = premiumPrice != null", self.discover)

    def test_onboarding_is_compact_screen_safe(self):
        self.assertIn(".verticalScroll(rememberScrollState())", self.entry)
        self.assertIn(".navigationBarsPadding()", self.entry)

    def test_first_run_copy_states_local_profile_and_non_diagnostic_scope(self):
        self.assertIn("your profile stays on this device", self.en)
        self.assertIn("not diagnosis", self.en)
        self.assertIn("ton profil reste sur cet appareil", self.fr)
        self.assertIn("pas pour établir un diagnostic", self.fr)


if __name__ == "__main__":
    unittest.main()
