from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class RenderingPerformanceContractTest(unittest.TestCase):
    def setUp(self):
        self.rendering = (ROOT / 'baseline-profile/src/main/java/com/whoareyou/app/baselineprofile/RenderingBenchmark.kt').read_text(encoding='utf-8')
        self.discover = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt').read_text(encoding='utf-8')
        self.quiz_visual = (ROOT / 'app/src/main/java/com/whoareyou/app/QuizVisualUi.kt').read_text(encoding='utf-8')
        self.onboarding = (ROOT / 'app/src/main/java/com/whoareyou/app/EntryScreensUi.kt').read_text(encoding='utf-8')
        self.result_share = (ROOT / 'app/src/main/java/com/whoareyou/app/ResultShare.kt').read_text(encoding='utf-8')

    def test_macrobenchmark_measures_discover_and_quiz_result_frames(self):
        self.assertIn('FrameTimingMetric()', self.rendering)
        self.assertIn('fun discoverScrollFrames()', self.rendering)
        self.assertIn('fun quizToResultFrames()', self.rendering)
        self.assertIn('By.res("discover_list")', self.rendering)
        self.assertIn('By.res("result_score")', self.rendering)

    def test_discover_idle_has_no_perpetual_ambient_animation(self):
        self.assertNotIn('rememberInfiniteTransition', self.discover)
        self.assertNotIn('infiniteRepeatable', self.discover)

    def test_remaining_ambient_animation_state_is_read_in_graphics_layer(self):
        for source in (self.quiz_visual, self.onboarding):
            self.assertIn('?.value ?: 1f', source)
        self.assertNotIn('val glowScale: Float', self.quiz_visual)

    def test_result_share_no_longer_reparses_quiz_catalog(self):
        self.assertNotIn('QuizRepository.load(context)', self.result_share)
        self.assertIn('quizId: String', self.result_share)

if __name__ == '__main__':
    unittest.main()
