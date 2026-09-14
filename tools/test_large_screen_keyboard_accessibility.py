from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class LargeScreenAndKeyboardAccessibilityTest(unittest.TestCase):
    def setUp(self):
        self.adaptive = (ROOT / 'app/src/main/java/com/whoareyou/app/AdaptiveLayoutUi.kt').read_text(encoding='utf-8')
        self.shell = (ROOT / 'app/src/main/java/com/whoareyou/app/AppShellUi.kt').read_text(encoding='utf-8')
        self.library = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverLibraryUi.kt').read_text(encoding='utf-8')
        self.theme = (ROOT / 'app/src/main/java/com/whoareyou/app/AppThemeUi.kt').read_text(encoding='utf-8')
        self.entry = (ROOT / 'app/src/main/java/com/whoareyou/app/EntryScreensUi.kt').read_text(encoding='utf-8')

    def test_readable_width_is_centered_and_bounded(self):
        self.assertIn('wrapContentWidth(Alignment.CenterHorizontally)', self.adaptive)
        self.assertIn('widthIn(max = 840.dp)', self.adaptive)

    def test_keyboard_focus_is_visible_on_shell_and_filters(self):
        self.assertIn('.onFocusChanged { focused = it.isFocused }', self.shell)
        self.assertIn('width = if (focused) 2.dp else 0.dp', self.shell)
        self.assertIn('.onFocusChanged { focused = it.isFocused }', self.library)

    def test_accent_foregrounds_use_dark_ink_for_contrast(self):
        self.assertIn('onPrimary = V2Colors.Ink', self.theme)
        self.assertIn('onSecondary = V2Colors.Ink', self.theme)
        self.assertIn('onTertiary = V2Colors.Ink', self.theme)

    def test_catalog_failure_can_scroll_with_large_fonts(self):
        catalog_block = self.entry.split('fun CatalogUnavailableScreen()', 1)[1].split('@Composable\nfun OnboardingScreen', 1)[0]
        self.assertIn('.verticalScroll(rememberScrollState())', catalog_block)

if __name__ == '__main__':
    unittest.main()
