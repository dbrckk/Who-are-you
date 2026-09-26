import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
PAIRS = [
    ("quizzes.json", "quizzes-fr.json"),
    ("quizzes-extra.json", "quizzes-extra-fr.json"),
    ("quizzes-growth.json", "quizzes-growth-fr.json"),
    ("quizzes-growth2.json", "quizzes-growth2-fr.json"),
    ("quizzes-growth3.json", "quizzes-growth3-fr.json"),
]

REQUIRED_BRANCHES = ("low", "balanced", "high")
REQUIRED_FIELDS = ("strengths", "watchOuts", "everydayLife", "reflection")


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))["quizzes"]


class ResultIntelligenceCatalogTest(unittest.TestCase):
    def test_all_quizzes_have_complete_result_intelligence_with_en_fr_parity(self):
        for en_name, fr_name in PAIRS:
            en = {q["id"]: q for q in load(ASSETS / en_name)}
            fr = {q["id"]: q for q in load(ASSETS / fr_name)}
            self.assertEqual(set(en), set(fr), f"quiz IDs differ in {en_name}/{fr_name}")

            for quiz_id in sorted(en):
                for locale, quiz in (("en", en[quiz_id]), ("fr", fr[quiz_id])):
                    with self.subTest(file=en_name, quiz=quiz_id, locale=locale):
                        intelligence = quiz.get("resultIntelligence")
                        self.assertIsInstance(intelligence, dict)
                        self.assertEqual(set(REQUIRED_BRANCHES), set(intelligence))
                        for branch in REQUIRED_BRANCHES:
                            payload = intelligence[branch]
                            self.assertEqual(set(REQUIRED_FIELDS), set(payload))
                            self.assertTrue(1 <= len(payload["strengths"]) <= 3)
                            self.assertTrue(1 <= len(payload["watchOuts"]) <= 3)
                            self.assertTrue(1 <= len(payload["everydayLife"]) <= 3)
                            self.assertIsInstance(payload["reflection"], str)
                            self.assertTrue(payload["reflection"].strip())


if __name__ == "__main__":
    unittest.main()
