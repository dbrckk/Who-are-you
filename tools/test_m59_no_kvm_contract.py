from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"


class M59NoKvmContractTest(unittest.TestCase):
    def test_device_validation_disables_linux_hardware_acceleration(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("disable-linux-hw-accel: true", workflow)


if __name__ == "__main__":
    unittest.main()
