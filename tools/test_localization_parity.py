from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"

FORMAT = re.compile(r"%(?:\d+\$)?[a-zA-Z]")


def strings_for(folder):
    values = {}
    for path in sorted((RES / folder).glob("*.xml")):
        root = ET.parse(path).getroot()
        for node in root.findall("string"):
            name = node.attrib.get("name")
            if not name or node.attrib.get("translatable") == "false":
                continue
            text = "".join(node.itertext())
            values[name] = (text, path.name)
    return values


class LocalizationParityContractTest(unittest.TestCase):
    def test_french_covers_all_translatable_english_strings(self):
        en = strings_for("values")
        fr = strings_for("values-fr")
        missing = sorted(set(en) - set(fr))
        self.assertEqual([], missing, f"Missing French strings: {missing}")

    def test_format_placeholders_match_between_locales(self):
        en = strings_for("values")
        fr = strings_for("values-fr")
        mismatches = []
        for name in sorted(set(en) & set(fr)):
            en_tokens = sorted(FORMAT.findall(en[name][0]))
            fr_tokens = sorted(FORMAT.findall(fr[name][0]))
            if en_tokens != fr_tokens:
                mismatches.append((name, en_tokens, fr_tokens))
        self.assertEqual([], mismatches, f"Format placeholder mismatch: {mismatches}")

if __name__ == "__main__":
    unittest.main()
