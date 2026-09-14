from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class LocalProfilePrivacyContractTest(unittest.TestCase):
    def test_android_backup_is_disabled_for_local_profile(self):
        manifest = (ROOT / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
        self.assertIn('android:allowBackup="false"', manifest)
        self.assertNotIn('android:allowBackup="true"', manifest)

if __name__ == '__main__':
    unittest.main()
