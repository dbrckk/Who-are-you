from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class AccessibilityLaunchContractTest(unittest.TestCase):
    def setUp(self):
        self.personalized = (ROOT / "app/src/main/java/com/whoareyou/app/PersonalizedDiscoverUi.kt").read_text(encoding="utf-8")
        self.library = (ROOT / "app/src/main/java/com/whoareyou/app/DiscoverLibraryUi.kt").read_text(encoding="utf-8")
        self.interactive = (ROOT / "app/src/main/java/com/whoareyou/app/V2InteractiveUi.kt").read_text(encoding="utf-8")

    def test_profile_dashboard_has_button_role(self):
        self.assertIn("clickable(role = Role.Button, onClick = onOpenProfile)", self.personalized)

    def test_library_filters_expose_button_and_selection_semantics(self):
        self.assertIn("clickable(role = Role.Button, onClick = onClick)", self.library)
        self.assertIn("semantics { this.selected = selected }", self.library)

    def test_shared_pressable_surface_exposes_enabled_semantics(self):
        self.assertIn("enabled = enabled", self.interactive)


if __name__ == "__main__":
    unittest.main()
