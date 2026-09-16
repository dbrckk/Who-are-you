from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"


def visual_job(workflow: str) -> str:
    return workflow[workflow.index("  visual_validation:") :]


class M59HardwareAccelerationContractTest(unittest.TestCase):
    def test_visual_validation_keeps_linux_hardware_acceleration(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")
        visual = visual_job(workflow)

        # M764 proved -accel off takes ~13 minutes to boot and then loses ADB
        # before validation starts. The split visual job must keep KVM enabled.
        self.assertNotIn("disable-linux-hw-accel: true", visual)


if __name__ == "__main__":
    unittest.main()
