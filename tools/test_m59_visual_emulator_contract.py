from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"


class M59VisualEmulatorContractTest(unittest.TestCase):
    def test_visual_validation_uses_rendering_capable_system_image(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        # Automated Test Device images disable hardware rendering and therefore
        # cannot be used as the source of screenshot-based visual evidence.
        self.assertNotIn("target: aosp_atd", workflow)
        self.assertNotIn("target: google_atd", workflow)
        self.assertIn("target: google_apis", workflow)


if __name__ == "__main__":
    unittest.main()
