from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"
DEVICE_SCRIPT = ROOT / ".github" / "scripts" / "android-device-validation.sh"
STRESS_SCRIPT = ROOT / ".github" / "scripts" / "android-runtime-stress.sh"


class M59RuntimeStressSplitContractTest(unittest.TestCase):
    def test_atd_job_owns_runtime_stress(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")
        instrumentation, visual = workflow.split("  visual_validation:", maxsplit=1)

        self.assertIn("target: aosp_atd", instrumentation)
        self.assertIn("bash .github/scripts/android-runtime-stress.sh", instrumentation)
        self.assertNotIn("android-runtime-stress.sh", visual)

    def test_visual_validation_skips_random_monkey_stress(self):
        script = DEVICE_SCRIPT.read_text(encoding="utf-8")

        self.assertIn('if [[ "$VALIDATION_MODE" != "visual" ]]; then', script)
        self.assertIn("run_monkey_stress", script)

    def test_runtime_stress_script_preserves_debug_and_candidate_coverage(self):
        self.assertTrue(STRESS_SCRIPT.is_file())
        script = STRESS_SCRIPT.read_text(encoding="utf-8")

        self.assertIn(":app:assembleDebug", script)
        self.assertIn(":app:assembleCandidate", script)
        self.assertIn("--ignore-crashes", script)
        self.assertIn("--ignore-timeouts", script)
        self.assertIn("--ignore-security-exceptions", script)
        self.assertIn("150", script)
        self.assertIn("REASON_(CRASH|ANR)", script)
        self.assertIn('stress_apk "$DEBUG_APK" debug-stress', script)
        self.assertIn('stress_apk "$CANDIDATE_APK" candidate-stress', script)

    def test_runtime_stress_captures_startup_evidence_before_pid_assertion(self):
        script = STRESS_SCRIPT.read_text(encoding="utf-8")

        self.assertIn("capture_runtime_evidence()", script)
        self.assertIn('pid="$(adb shell pidof "$PACKAGE" | tr -d \'\\r\' || true)"', script)
        self.assertIn('final_pid="$(adb shell pidof "$PACKAGE" | tr -d \'\\r\' || true)"', script)

        start = script.index('start_output="$(adb shell am start -W -n "$ACTIVITY")"')
        first_capture = script.index('capture_runtime_evidence "$label"', start)
        pid_lookup = script.index('pid="$(adb shell pidof', start)
        self.assertLess(first_capture, pid_lookup)


if __name__ == "__main__":
    unittest.main()
