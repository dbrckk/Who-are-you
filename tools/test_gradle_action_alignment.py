from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOWS = ROOT / ".github/workflows"


class GradleActionAlignmentTest(unittest.TestCase):
    def test_all_android_workflows_use_setup_gradle_v6(self):
        offenders = []
        for path in sorted(WORKFLOWS.glob("*.yml")):
            source = path.read_text(encoding="utf-8")
            if "gradle/actions/setup-gradle@" not in source:
                continue
            if "gradle/actions/setup-gradle@v6" not in source:
                offenders.append(path.name)
            self.assertNotIn(
                "gradle/actions/setup-gradle@v4",
                source,
                f"stale setup-gradle action in {path.name}",
            )
        self.assertEqual([], offenders)


if __name__ == "__main__":
    unittest.main()
