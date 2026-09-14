from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ShellAccessibilityContractTest(unittest.TestCase):
    def setUp(self):
        self.shell = (ROOT / 'app/src/main/java/com/whoareyou/app/AppShellUi.kt').read_text(encoding='utf-8')
        self.back = (ROOT / 'app/src/main/java/com/whoareyou/app/AccessibilityUi.kt').read_text(encoding='utf-8')

    def test_shell_respects_reduced_motion(self):
        self.assertIn('val reduceMotion = reducedMotionEnabled()', self.shell)
        self.assertIn('if (reduceMotion) snap()', self.shell)

    def test_shell_height_can_expand_for_large_fonts(self):
        self.assertIn('.heightIn(min = 56.dp)', self.shell)
        self.assertNotIn('.height(56.dp)', self.shell)

    def test_back_action_uses_shared_accessible_type_scale(self):
        self.assertIn('style = V2Type.Supporting', self.back)
        self.assertNotIn('fontSize = 13.sp', self.back)

if __name__ == '__main__':
    unittest.main()
