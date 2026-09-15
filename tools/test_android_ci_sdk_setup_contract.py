from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW_PATHS = (
    ROOT / '.github/workflows/android-ci.yml',
    ROOT / '.github/workflows/android-apk.yml',
)


class AndroidCiSdkSetupContractTest(unittest.TestCase):
    def test_setup_android_skips_removed_legacy_tools_package(self):
        for path in WORKFLOW_PATHS:
            with self.subTest(workflow=path.name):
                workflow = path.read_text(encoding='utf-8')
                setup_block = workflow.split(
                    'uses: android-actions/setup-android@v4',
                    1,
                )[1].split('- name: Install Android SDK packages', 1)[0]

                self.assertIn('with:', setup_block)
                self.assertIn("packages: ''", setup_block)


if __name__ == '__main__':
    unittest.main()
