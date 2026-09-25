from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
RESOURCE_FILES = [
    ROOT / "app/src/main/res/values/strings.xml",
    ROOT / "app/src/main/res/values-fr/strings.xml",
]

FORBIDDEN = {
    "diagnosis",
    "diagnostic",
    "addiction",
    "addicted",
    "personality",
    "depression",
    "anxiety",
    "lazy",
    "unhealthy",
    "diagnostic",
    "dépendance",
    "dépendant",
    "personnalité",
    "dépression",
    "anxiété",
    "paresseux",
    "malsain",
}


class BehaviorCopyContractTest(unittest.TestCase):
    def test_habits_copy_stays_non_clinical_and_non_moralizing(self):
        for path in RESOURCE_FILES:
            source = path.read_text(encoding="utf-8")
            habits_copy = " ".join(
                match.group(1)
                for match in re.finditer(
                    r'<string name="habits_[^"]+">(.*?)</string>',
                    source,
                    flags=re.DOTALL,
                )
            ).lower()

            for word in FORBIDDEN:
                self.assertNotIn(word, habits_copy, f"{word} in {path}")


if __name__ == "__main__":
    unittest.main()
