from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github" / "scripts" / "android-device-validation.sh"
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"


class M59HostDiagnosticsContractTest(unittest.TestCase):
    def test_device_validation_captures_host_resource_pressure_during_instrumentation(self):
        script = SCRIPT.read_text(encoding="utf-8")

        self.assertIn("device-host-resources.txt", script)
        self.assertIn("/sys/fs/cgroup/memory.current", script)
        self.assertIn("/sys/fs/cgroup/memory.max", script)
        self.assertIn("/sys/fs/cgroup/memory.events", script)
        self.assertIn("/proc/pressure/memory", script)
        self.assertIn("start_host_resource_monitor", script)
        self.assertLess(
            script.index("start_host_resource_monitor"),
            script.index("gradle :app:connectedDebugAndroidTest"),
        )

    def test_device_validation_uploads_host_diagnostics(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")

        self.assertIn("device-host-resources.txt", workflow)
        self.assertIn("device-host-kernel.txt", workflow)


if __name__ == "__main__":
    unittest.main()
