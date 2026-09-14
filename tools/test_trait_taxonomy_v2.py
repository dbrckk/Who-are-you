import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"

EN_FILES = [
    "quizzes.json",
    "quizzes-extra.json",
    "quizzes-growth.json",
    "quizzes-growth2.json",
    "quizzes-growth3.json",
]
FR_FILES = [
    "quizzes-fr.json",
    "quizzes-extra-fr.json",
    "quizzes-growth-fr.json",
    "quizzes-growth2-fr.json",
    "quizzes-growth3-fr.json",
]


def load(files):
    quizzes = []
    for name in files:
        data = json.loads((ASSETS / name).read_text(encoding="utf-8"))
        quizzes.extend(data["quizzes"])
    return quizzes


class TraitTaxonomyV2ContractTest(unittest.TestCase):
    def test_all_catalogs_are_v2(self):
        for name in EN_FILES + FR_FILES:
            data = json.loads((ASSETS / name).read_text(encoding="utf-8"))
            self.assertEqual(2, data["version"], name)

    def test_all_30_quizzes_have_explicit_valid_traits(self):
        quizzes = load(EN_FILES)
        self.assertEqual(30, len(quizzes))
        for quiz in quizzes:
            self.assertTrue(quiz.get("traits"), quiz["id"])
            for trait in quiz["traits"]:
                self.assertTrue(trait["id"])
                self.assertNotEqual(0, trait["weight"])
                self.assertGreaterEqual(trait["weight"], -1)
                self.assertLessEqual(trait["weight"], 1)

    def test_french_catalog_has_exact_id_and_taxonomy_parity(self):
        en = {q["id"]: q for q in load(EN_FILES)}
        fr = {q["id"]: q for q in load(FR_FILES)}
        self.assertEqual(set(en), set(fr))
        for quiz_id in en:
            self.assertEqual(en[quiz_id]["traits"], fr[quiz_id]["traits"], quiz_id)

    def test_growth_catalogs_are_loaded_by_repository(self):
        source = (ROOT / "app/src/main/java/com/whoareyou/app/QuizCatalog.kt").read_text(encoding="utf-8")
        for name in ("quizzes-growth.json", "quizzes-growth2.json", "quizzes-growth3.json"):
            self.assertIn(f'"{name}"', source)


if __name__ == "__main__":
    unittest.main()
