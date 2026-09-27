from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
ANDROID_CI = ROOT / ".github/workflows/android-ci.yml"
DEVICE_CI = ROOT / ".github/workflows/m59-device-validation.yml"
SEMANTIC_WORKFLOW = "Precise semantic refresh"


class PostSemanticRefreshValidationContractTest(unittest.TestCase):
    def assert_revalidates_after_semantic_refresh(self, path: Path):
        source = path.read_text(encoding="utf-8")
        self.assertIn("workflow_run:", source)
        self.assertIn(f"- {SEMANTIC_WORKFLOW}", source)
        self.assertIn("types:", source)
        self.assertIn("- completed", source)
        self.assertIn("github.event_name != 'workflow_run'", source)
        self.assertIn("github.event.workflow_run.conclusion == 'success'", source)

    def test_android_ci_revalidates_main_after_semantic_refresh(self):
        self.assert_revalidates_after_semantic_refresh(ANDROID_CI)

    def test_device_validation_revalidates_main_after_semantic_refresh(self):
        self.assert_revalidates_after_semantic_refresh(DEVICE_CI)


if __name__ == "__main__":
    unittest.main()
