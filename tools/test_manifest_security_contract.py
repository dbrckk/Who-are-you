from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID_NS = "http://schemas.android.com/apk/res/android"


class ManifestSecurityContractTest(unittest.TestCase):
    def setUp(self):
        self.manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        self.file_paths = (ROOT / "app/src/main/res/xml/file_paths.xml").read_text(encoding="utf-8")
        self.billing = (ROOT / "app/src/main/java/com/whoareyou/app/BillingManager.kt").read_text(encoding="utf-8")
        self.ads = (ROOT / "app/src/main/java/com/whoareyou/app/AdManager.kt").read_text(encoding="utf-8")

    def test_manifest_uses_only_approved_permissions_and_secure_defaults(self):
        approved = {
            "android.permission.INTERNET",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.health.READ_STEPS",
        }
        root = ET.fromstring(self.manifest)
        declared = {
            permission.attrib[f"{{{ANDROID_NS}}}name"]
            for permission in root.findall("uses-permission")
        }
        self.assertEqual(approved, declared)
        self.assertNotIn("android.permission.READ_CALL_LOG", self.manifest)
        self.assertNotIn("android.permission.READ_SMS", self.manifest)
        self.assertNotIn("android.permission.READ_CONTACTS", self.manifest)
        self.assertNotIn("android.permission.ACCESS_FINE_LOCATION", self.manifest)
        self.assertNotIn("android.permission.ACCESS_COARSE_LOCATION", self.manifest)
        self.assertIn('android:allowBackup="false"', self.manifest)
        self.assertIn('android:usesCleartextTraffic="false"', self.manifest)

    def test_file_provider_is_not_exported_and_paths_are_narrow(self):
        self.assertIn('android:name="androidx.core.content.FileProvider"', self.manifest)
        self.assertIn('android:exported="false"', self.manifest)
        self.assertIn('<cache-path name="shared_results" path="shared_results/" />', self.file_paths)
        self.assertNotIn("<root-path", self.file_paths)
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
