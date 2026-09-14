from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class RuntimeEfficiencyContractTest(unittest.TestCase):
    def setUp(self):
        self.catalog = (ROOT / 'app/src/main/java/com/whoareyou/app/QuizCatalog.kt').read_text(encoding='utf-8')
        self.main = (ROOT / 'app/src/main/java/com/whoareyou/app/MainActivity.kt').read_text(encoding='utf-8')
        self.ads = (ROOT / 'app/src/main/java/com/whoareyou/app/AdManager.kt').read_text(encoding='utf-8')
        self.discover = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt').read_text(encoding='utf-8')

    def test_quiz_catalog_cache_returns_before_asset_checks(self):
        load = self.catalog.split('fun load(context: Context)', 1)[1].split('fun find(', 1)[0]
        cache_index = load.index('cached?.takeIf { cachedLanguage == language }?.let { return it }')
        asset_index = load.index('assetExists(appContext, localizedName)')
        self.assertLess(cache_index, asset_index)

    def test_premium_users_do_not_create_ad_manager(self):
        self.assertIn('BuildConfig.EXTERNAL_SERVICES_ENABLED && !adsRemoved', self.main)
        self.assertIn('DisposableEffect(adManager)', self.main)
        self.assertIn('adManager?.close()', self.main)

    def test_discover_sections_use_stable_lazy_keys(self):
        for key in ('hero', 'personalized', 'collections', 'library', 'footer'):
            self.assertIn(f'key = "{key}"', self.discover)

    def test_ad_manager_has_explicit_release_path(self):
        self.assertIn('fun close()', self.ads)
        self.assertIn('interstitial = null', self.ads)

if __name__ == '__main__':
    unittest.main()
