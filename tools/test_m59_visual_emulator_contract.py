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

    def test_visual_validation_uses_supported_software_renderer(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        # Emulator 36.4.9 deprecated swiftshader_indirect. M59 runs headless on
        # current stable Emulator builds, so keep the visual gate on a supported
        # software renderer rather than the legacy indirect backend.
        self.assertNotIn("-gpu swiftshader_indirect", workflow)
        self.assertIn("-gpu software", workflow)

    def test_visual_validation_pins_known_stable_emulator_build(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        # Emulator 37.1.11 repeatedly disappeared from ADB during the first
        # connected UI test. Pin the final stable 36.x patch used by this gate.
        self.assertIn("emulator-build: '15004761'", workflow)


if __name__ == "__main__":
    unittest.main()
