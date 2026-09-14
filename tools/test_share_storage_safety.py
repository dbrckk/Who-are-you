from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ShareStorageSafetyContractTest(unittest.TestCase):
    def setUp(self):
        self.store = (ROOT / 'app/src/main/java/com/whoareyou/app/ShareFileStore.kt').read_text(encoding='utf-8')
        self.paths = (ROOT / 'app/src/main/res/xml/file_paths.xml').read_text(encoding='utf-8')
        self.result = (ROOT / 'app/src/main/java/com/whoareyou/app/ResultShare.kt').read_text(encoding='utf-8')
        self.match = (ROOT / 'app/src/main/java/com/whoareyou/app/CompatibilityShare.kt').read_text(encoding='utf-8')
        self.profile = (ROOT / 'app/src/main/java/com/whoareyou/app/GlobalProfileShare.kt').read_text(encoding='utf-8')

    def test_share_cache_is_bounded_and_png_encoding_is_checked(self):
        self.assertIn('MAX_CACHED_SHARE_FILES = 8', self.store)
        self.assertIn('check(bitmap.compress', self.store)
        self.assertIn('.drop(MAX_CACHED_SHARE_FILES - 1)', self.store)

    def test_file_provider_exposes_only_share_subdirectory(self):
        self.assertIn('path="shared_results/"', self.paths)
        self.assertNotIn('path="."', self.paths)

    def test_all_image_shares_use_shared_file_store(self):
        for source in (self.result, self.match, self.profile):
            self.assertIn('ShareFileStore.writePng(', source)

if __name__ == '__main__':
    unittest.main()
