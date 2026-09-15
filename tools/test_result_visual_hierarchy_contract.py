from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class ResultVisualHierarchyContractTest(unittest.TestCase):
    def setUp(self):
        self.result = (ROOT / "app/src/main/java/com/whoareyou/app/ResultScreenUi.kt").read_text(encoding="utf-8")

    def test_score_card_has_stable_visual_anchor(self):
        self.assertIn('testTag("result_score_card")', self.result)

    def test_score_card_keeps_accessible_score_semantics(self):
        self.assertIn('testTag("result_score")', self.result)
        self.assertIn('contentDescription = scoreAccessibility', self.result)


if __name__ == "__main__":
    unittest.main()
