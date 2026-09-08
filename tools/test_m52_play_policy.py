import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]


class M52PlayPolicyTests(unittest.TestCase):
    def read(self, path: str) -> str:
        return (ROOT / path).read_text(encoding="utf-8")

    def test_ump_refreshes_and_exposes_privacy_options(self):
        ad = self.read("app/src/main/java/com/whoareyou/app/AdManager.kt")
        self.assertIn("requestConsentInfoUpdate", ad)
        self.assertIn("privacyOptionsRequirementStatus", ad)
        self.assertIn("showPrivacyOptionsForm", ad)
        self.assertIn("canRequestAds()", ad)
        self.assertNotIn("consentRequested", ad)

    def test_privacy_entry_point_is_user_visible_when_required(self):
        main = self.read("app/src/main/java/com/whoareyou/app/MainActivity.kt")
        hub = self.read("app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt")
        en = self.read("app/src/main/res/values/strings.xml")
        fr = self.read("app/src/main/res/values-fr/strings.xml")
        self.assertIn("privacyOptionsRequired", main)
        self.assertIn("onPrivacyOptions", main)
        self.assertIn("PrivacyOptionsCard", hub)
        for text in (en, fr):
            self.assertIn('name="privacy_options_title"', text)
            self.assertIn('name="privacy_options_button"', text)

    def test_manifest_is_minimal_and_hardened(self):
        manifest = self.read("app/src/main/AndroidManifest.xml")
        self.assertIn('android.permission.INTERNET', manifest)
        self.assertIn('android:allowBackup="false"', manifest)
        self.assertIn('android:usesCleartextTraffic="false"', manifest)
        self.assertNotIn('READ_CONTACTS', manifest)
        self.assertNotIn('ACCESS_FINE_LOCATION', manifest)
        self.assertNotIn('CAMERA', manifest)

    def test_file_provider_exports_only_shared_result_cache(self):
        manifest = self.read("app/src/main/AndroidManifest.xml")
        paths = self.read("app/src/main/res/xml/file_paths.xml")
        self.assertIn('android:exported="false"', manifest)
        self.assertIn('android:grantUriPermissions="true"', manifest)
        self.assertIn('<cache-path name="shared_results" path="shared_results/" />', paths)
        self.assertNotIn('<root-path', paths)
        self.assertNotIn('path="."', paths)

    def test_billing_uses_play_billing_and_acknowledges_purchases(self):
        billing = self.read("app/src/main/java/com/whoareyou/app/BillingManager.kt")
        self.assertIn('REMOVE_ADS_PRODUCT_ID = "remove_ads_lifetime"', billing)
        self.assertIn("Purchase.PurchaseState.PURCHASED", billing)
        self.assertIn("acknowledgePurchase", billing)
        self.assertIn("queryPurchasesAsync", billing)
        self.assertIn("enableOneTimeProducts", billing)

    def test_https_app_link_is_scoped_to_challenges(self):
        manifest = self.read("app/src/main/AndroidManifest.xml")
        self.assertIn('android:autoVerify="true"', manifest)
        self.assertIn('android:scheme="https"', manifest)
        self.assertIn('android:host="dbrckk.github.io"', manifest)
        self.assertIn('android:pathPrefix="/Who-are-you/challenge/"', manifest)


if __name__ == "__main__":
    unittest.main()
