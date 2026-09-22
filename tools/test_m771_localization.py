import unittest
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app" / "ResultIntelligenceUi.kt"
EN = ROOT / "app" / "src" / "main" / "res" / "values" / "strings.xml"
FR = ROOT / "app" / "src" / "main" / "res" / "values-fr" / "strings.xml"

KEYS = {
    "m771_why_result",
    "m771_strengths",
    "m771_watchouts",
    "m771_everyday_life",
    "m771_reflection",
    "m771_profile_connections",
    "m771_connection_reinforcing",
    "m771_connection_contrasting",
    "m771_connection_contextual",
}

def keys(path):
    root = ET.parse(path).getroot()
    return {node.attrib["name"] for node in root.findall("string")}

class M771LocalizationTest(unittest.TestCase):
    def test_m771_ui_copy_is_resource_backed_and_bilingual(self):
        source = UI.read_text(encoding="utf-8")
        en = keys(EN)
        fr = keys(FR)
        self.assertTrue(KEYS <= en)
        self.assertTrue(KEYS <= fr)
        for key in KEYS:
            self.assertIn(f"R.string.{key}", source)

if __name__ == "__main__":
    unittest.main()
