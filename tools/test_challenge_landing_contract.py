from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ChallengeLandingContractTest(unittest.TestCase):
    def setUp(self):
        self.page = (ROOT / 'docs/challenge/index.html').read_text(encoding='utf-8')
        self.manifest = (ROOT / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
        self.share = (ROOT / 'app/src/main/java/com/whoareyou/app/ChallengeShare.kt').read_text(encoding='utf-8')

    def test_landing_does_not_auto_launch_custom_scheme(self):
        self.assertNotIn('setTimeout(attemptOpen', self.page)
        self.assertNotIn('const attemptOpen', self.page)

    def test_invalid_challenge_hides_open_button(self):
        self.assertIn('openApp.hidden = true', self.page)
        self.assertIn('challenge link is no longer valid', self.page.lower())

    def test_keyboard_focus_is_visible(self):
        self.assertIn('a:focus-visible', self.page)

    def test_app_and_web_routes_stay_aligned(self):
        self.assertIn('android:host="dbrckk.github.io"', self.manifest)
        self.assertIn('android:pathPrefix="/Who-are-you/challenge/"', self.manifest)
        self.assertIn('private const val WEB_HOST = "dbrckk.github.io"', self.share)
        self.assertIn('private const val WEB_PATH = "/Who-are-you/challenge/"', self.share)

if __name__ == '__main__':
    unittest.main()
