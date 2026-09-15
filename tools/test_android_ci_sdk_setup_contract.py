from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = (ROOT / '.github/workflows/android-ci.yml').read_text(encoding='utf-8')


class AndroidCiSdkSetupContractTest(unittest.TestCase):
    def test_setup_android_skips_removed_legacy_tools_package(self):
        setup_block = WORKFLOW.split(
            'uses: android-actions/setup-android@v4',
            1,
        )[1].split('- name: Install Android SDK packages', 1)[0]

        self.assertIn('with:', setup_block)
        self.assertIn("packages: ''", setup_block)


if __name__ == '__main__':
    unittest.main()
