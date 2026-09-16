from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"
SCRIPT = ROOT / ".github" / "scripts" / "android-device-validation.sh"


def job_block(workflow: str, job_name: str, next_job_name: str | None = None) -> str:
    marker = f"  {job_name}:"
    self_start = workflow.index(marker)
    block = workflow[self_start:]
    if next_job_name is not None:
        next_marker = f"  {next_job_name}:"
        block = block[: block.index(next_marker)]
    return block


class M59SplitValidationContractTest(unittest.TestCase):
    def test_instrumentation_and_visual_validation_use_separate_emulators(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")
        instrumentation = job_block(workflow, "instrumentation_validation", "visual_validation")
        visual = job_block(workflow, "visual_validation")

        self.assertIn("target: aosp_atd", instrumentation)
        self.assertIn("VALIDATION_MODE=instrumentation", instrumentation)
        self.assertIn("target: google_apis", visual)
        self.assertIn("VALIDATION_MODE=visual", visual)
        self.assertNotIn("target: aosp_atd", visual)

    def test_visual_mode_skips_connected_instrumentation(self):
        script = SCRIPT.read_text(encoding="utf-8")

        self.assertIn('VALIDATION_MODE="${VALIDATION_MODE:-full}"', script)
        self.assertIn('if [[ "$VALIDATION_MODE" != "visual" ]]', script)
        self.assertIn('if [[ "$VALIDATION_MODE" == "instrumentation" ]]', script)
        self.assertIn("gradle :app:connectedDebugAndroidTest", script)


if __name__ == "__main__":
    unittest.main()
