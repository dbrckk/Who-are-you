from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"


def visual_job(workflow: str) -> str:
    return workflow[workflow.index("  visual_validation:") :]


class M59VisualEmulatorContractTest(unittest.TestCase):
    def test_visual_validation_uses_rendering_capable_system_image(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")
        visual = visual_job(workflow)

        # Automated Test Device images disable hardware rendering and therefore
        # cannot be used as the source of screenshot-based visual evidence.
        self.assertNotIn("target: aosp_atd", visual)
        self.assertNotIn("target: google_atd", visual)
        self.assertIn("target: google_apis", visual)

    def test_visual_validation_uses_supported_software_renderer(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")
        visual = visual_job(workflow)

        # Emulator 36.4.9 deprecated swiftshader_indirect. Keep the visual gate
        # on a supported software renderer rather than the legacy indirect backend.
        self.assertNotIn("-gpu swiftshader_indirect", visual)
        self.assertIn("-gpu software", visual)

    def test_visual_validation_pins_known_stable_emulator_build(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")
        visual = visual_job(workflow)

        # Keep rendered evidence on the final stable 36.x patch while the ATD
        # job independently owns connected instrumentation coverage.
        self.assertIn("emulator-build: '15004761'", visual)


if __name__ == "__main__":
    unittest.main()
