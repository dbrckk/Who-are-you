from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
RESULT_SCREEN = ROOT / "app/src/main/java/com/whoareyou/app/ResultScreenUi.kt"
INTELLIGENCE_UI = ROOT / "app/src/main/java/com/whoareyou/app/ResultIntelligenceUi.kt"
EN = ROOT / "app/src/main/res/values/m771-result-intelligence.xml"
FR = ROOT / "app/src/main/res/values-fr/m771-result-intelligence.xml"


class ResultIntelligenceUiContractTest(unittest.TestCase):
    def test_result_screen_wires_answer_trace_into_intelligence_panel(self):
        screen = RESULT_SCREEN.read_text(encoding="utf-8")
        self.assertIn("ResultIntelligencePanel(", screen)
        self.assertIn("selectedAnswerIndexes = selectedAnswerIndexes", screen)

    def test_intelligence_panel_and_bilingual_resources_exist(self):
        self.assertTrue(INTELLIGENCE_UI.exists(), "ResultIntelligenceUi.kt is required")
        self.assertTrue(EN.exists(), "English M771 resources are required")
        self.assertTrue(FR.exists(), "French M771 resources are required")

        ui = INTELLIGENCE_UI.read_text(encoding="utf-8")
        self.assertIn('testTag("result_intelligence_panel")', ui)
        self.assertIn("ResultIntelligenceEngine.derive", ui)

        def names(path: Path):
            text = path.read_text(encoding="utf-8")
            return set(re.findall(r'<string name="([^"]+)"', text))

        en_names = names(EN)
        fr_names = names(FR)
        self.assertEqual(en_names, fr_names)
        self.assertGreaterEqual(len(en_names), 12)


if __name__ == "__main__":
    unittest.main()
