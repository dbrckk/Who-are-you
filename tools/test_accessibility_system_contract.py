from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class AccessibilitySystemContractTest(unittest.TestCase):
    def setUp(self):
        self.interactive = (ROOT / 'app/src/main/java/com/whoareyou/app/V2InteractiveUi.kt').read_text(encoding='utf-8')
        self.design = (ROOT / 'app/src/main/java/com/whoareyou/app/V2DesignSystem.kt').read_text(encoding='utf-8')
        self.library = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverLibraryUi.kt').read_text(encoding='utf-8')

    def test_shared_click_targets_are_at_least_52dp(self):
        self.assertGreaterEqual(self.interactive.count('.heightIn(min = 52.dp)'), 2)

    def test_supporting_text_baseline_is_not_tiny(self):
        self.assertIn('fontSize = 14.sp', self.design)
        self.assertIn('fontSize = 12.sp', self.design)

    def test_library_filters_have_48dp_target(self):
        self.assertIn('.heightIn(min = 48.dp)', self.library)

    def test_library_result_content_is_not_forced_to_two_lines(self):
        block = self.library.split('private fun LibraryResultCard', 1)[1]
        self.assertNotIn('maxLines = 2', block)

    def test_library_title_is_exposed_as_heading(self):
        self.assertIn('import androidx.compose.ui.semantics.heading', self.library)
        title_block = self.library.split(
            'stringResource(R.string.library_title)',
            1,
        )[1].split('Spacer(Modifier.height(5.dp))', 1)[0]
        self.assertIn('Modifier.semantics { heading() }', title_block)

if __name__ == '__main__':
    unittest.main()
