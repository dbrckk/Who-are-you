import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VALUES = ROOT / "app/src/main/res/values/strings.xml"
VALUES_FR = ROOT / "app/src/main/res/values-fr/strings.xml"

NAME_RE = re.compile(r'<string\s+name="([^"]+)"')


def resource_names(path: Path) -> set[str]:
    return set(NAME_RE.findall(path.read_text(encoding="utf-8")))


class StringResourceParityTest(unittest.TestCase):
    def test_french_catalog_matches_default_string_keys(self):
        default = resource_names(VALUES)
        french = resource_names(VALUES_FR)
        self.assertEqual(default, french, (
            f"String resource parity mismatch. "
            f"Missing in FR: {sorted(default - french)}; "
            f"FR-only: {sorted(french - default)}"
        ))

    def test_accessibility_pane_title_resources_exist_in_both_locales(self):
        required = {"discover_headline", "your_profile"}
        default = resource_names(VALUES)
        french = resource_names(VALUES_FR)
        self.assertTrue(required <= default)
        self.assertTrue(required <= french)


    def test_main_activity_string_references_exist(self):
        source = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
        referenced = set(re.findall(r"R\\.string\\.([A-Za-z0-9_]+)", source))
        available = resource_names(VALUES)
        self.assertFalse(
            referenced - available,
            f"MainActivity references missing strings: {sorted(referenced - available)}",
        )


if __name__ == "__main__":
    unittest.main()
