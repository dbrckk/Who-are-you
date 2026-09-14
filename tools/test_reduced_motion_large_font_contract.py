from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ReducedMotionAndLargeFontContractTest(unittest.TestCase):
    def setUp(self):
        self.motion = (ROOT / 'app/src/main/java/com/whoareyou/app/V2MotionPreferences.kt').read_text(encoding='utf-8')
        self.entry = (ROOT / 'app/src/main/java/com/whoareyou/app/EntryScreensUi.kt').read_text(encoding='utf-8')
        self.discover = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt').read_text(encoding='utf-8')
        self.interactive = (ROOT / 'app/src/main/java/com/whoareyou/app/V2InteractiveUi.kt').read_text(encoding='utf-8')
        self.result = (ROOT / 'app/src/main/java/com/whoareyou/app/ResultScreenUi.kt').read_text(encoding='utf-8')
        self.profile = (ROOT / 'app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt').read_text(encoding='utf-8')
        self.collections = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverCollectionsUi.kt').read_text(encoding='utf-8')

    def test_android_animation_scale_is_respected(self):
        self.assertIn('Settings.Global.ANIMATOR_DURATION_SCALE', self.motion)
        self.assertIn('reducedMotionEnabled()', self.entry)
        self.assertIn('reducedMotionEnabled()', self.interactive)
        self.assertIn('reducedMotionEnabled()', self.result)
        self.assertIn('reducedMotionEnabled()', self.profile)

    def test_reduced_motion_uses_snap_for_micro_interactions(self):
        self.assertIn('if (reduceMotion) snap()', self.interactive)
        self.assertIn('if (reduceMotion) snap()', self.result)
        self.assertIn('if (reduceMotion) snap()', self.profile)

    def test_editorial_cards_do_not_force_two_line_truncation(self):
        block = self.collections.split('private fun EditorialCard', 1)[1]
        self.assertNotIn('maxLines = 2', block)

if __name__ == '__main__':
    unittest.main()
