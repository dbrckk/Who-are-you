from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
CHECKLIST = ROOT / "docs/internal-test-smoke-test.md"


class HabitsReleaseSmokeChecklistTest(unittest.TestCase):
    def test_smoke_checklist_covers_local_habits_and_goals(self):
        source = CHECKLIST.read_text(encoding="utf-8").lower()

        required = (
            "local-only",
            "health connect",
            "usage access",
            "permission denial",
            "permission revocation",
            "today / 7-day / 30-day",
            "create, pause, resume and remove a local goal",
            "goal persists after relaunch",
            "full habits reset",
            "questionnaire profile remains intact",
            "130% font",
        )
        for phrase in required:
            self.assertIn(phrase, source)


if __name__ == "__main__":
    unittest.main()
