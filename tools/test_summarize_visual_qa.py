import importlib.util
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github" / "scripts" / "summarize-visual-qa.py"

spec = importlib.util.spec_from_file_location("summarize_visual_qa", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec.loader is not None
spec.loader.exec_module(module)


def write_case(root: Path, label: str, screen_ok: bool = True, ui_ok: bool = True):
    (root / f"device-screen-{label}.png").write_bytes(b"png")
    (root / f"device-ui-{label}.xml").write_text("<hierarchy/>", encoding="utf-8")
    screen_text = (
        "Screenshot integrity OK: 720x1600px, 12000 bytes, lumaRange=180, colorBuckets=32"
        if screen_ok
        else "Screenshot appears blank or nearly uniform"
    )
    ui_text = (
        "UI hierarchy: 20 total nodes, 18 app nodes, 4 clickable"
        if ui_ok
        else "UI hierarchy: 20 total nodes\nClickable nodes below 48dp touch target:"
    )
    (root / f"device-screen-report-{label}.txt").write_text(screen_text, encoding="utf-8")
    (root / f"device-ui-report-{label}.txt").write_text(ui_text, encoding="utf-8")


class VisualQaSummaryTests(unittest.TestCase):
    def test_pass_summary(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            write_case(root, "candidate")
            output = root / "summary.md"
            result = module.main(root, output)
            self.assertEqual(result, 0)
            text = output.read_text(encoding="utf-8")
            self.assertIn("| candidate | PASS |", text)
            self.assertIn("Failures: 0", text)

    def test_failure_summary(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            write_case(root, "candidate", screen_ok=False)
            output = root / "summary.md"
            result = module.main(root, output)
            self.assertEqual(result, 1)
            text = output.read_text(encoding="utf-8")
            self.assertIn("| candidate | FAIL |", text)
            self.assertIn("Failures: 1", text)

    def test_ui_failure_summary(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            write_case(root, "compact", ui_ok=False)
            output = root / "summary.md"
            result = module.main(root, output)
            self.assertEqual(result, 1)
            self.assertIn("compact", output.read_text(encoding="utf-8"))

    def test_no_reports_fails(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            output = root / "summary.md"
            self.assertEqual(module.main(root, output), 1)


if __name__ == "__main__":
    unittest.main()
