from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ManifestSecurityContractTest(unittest.TestCase):
    def setUp(self):
        self.manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        self.file_paths = (ROOT / "app/src/main/res/xml/file_paths.xml").read_text(encoding="utf-8")
        self.billing = (ROOT / "app/src/main/java/com/whoareyou/app/BillingManager.kt").read_text(encoding="utf-8")
        self.ads = (ROOT / "app/src/main/java/com/whoareyou/app/AdManager.kt").read_text(encoding="utf-8")

    def test_manifest_uses_minimal_network_permission_and_secure_defaults(self):
        self.assertIn('android.permission.INTERNET', self.manifest)
        self.assertEqual(1, self.manifest.count("<uses-permission"))
        self.assertIn('android:allowBackup="false"', self.manifest)
        self.assertIn('android:usesCleartextTraffic="false"', self.manifest)

    def test_file_provider_is_not_exported_and_paths_are_narrow(self):
        self.assertIn('android:name="androidx.core.content.FileProvider"', self.manifest)
        self.assertIn('android:exported="false"', self.manifest)
        self.assertIn('<cache-path name="shared_results" path="shared_results/" />', self.file_paths)
        self.assertNotIn('<root-path', self.file_paths)
        self.assertNotIn('path="."', self.file_paths)

    def test_billing_restore_revokes_missing_entitlement(self):
        self.assertIn("val hasActivePremium = purchases.any", self.billing)
        self.assertIn("onPremiumChanged(false)", self.billing)
        self.assertIn("ProfileStore.setAdsRemoved(appContext, false)", self.billing)

    def test_top_level_compose_surfaces_handle_status_bar_insets(self):
        paths = [
            "DiscoverHubUi.kt",
            "ProfileScreenUi.kt",
            "QuizScreenUi.kt",
            "ResultScreenUi.kt",
            "ChallengeUi.kt",
        ]
        for name in paths:
            source = (
                ROOT / "app/src/main/java/com/whoareyou/app" / name
            ).read_text(encoding="utf-8")
            self.assertIn("statusBarsPadding()", source, name)

    def test_ad_show_failure_preloads_replacement(self):
        marker = "override fun onAdFailedToShowFullScreenContent"
        block = self.ads[self.ads.index(marker):]
        block = block[:block.index("override fun onAdImpression")]
        self.assertIn("load()", block)
        self.assertIn("onContinue()", block)

if __name__ == "__main__":
    unittest.main()
