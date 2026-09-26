from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"
EN = ROOT / "app/src/main/res/values/strings.xml"
FR = ROOT / "app/src/main/res/values-fr/strings.xml"


class BehaviorGoalV1ScopeContractTest(unittest.TestCase):
    def read(self, name: str) -> str:
        return (APP / name).read_text(encoding="utf-8")

    def test_v1_has_no_goal_editing_surface(self):
        integration = self.read("BehaviorGoalIntegrationUi.kt")
        ui = self.read("BehaviorGoalsUi.kt")

        self.assertNotIn("fun update(", integration)
        self.assertNotIn("onEdit:", integration)
        self.assertNotIn("onEdit =", integration)
        self.assertNotIn("onEdit:", ui)
        self.assertNotIn("editingGoal", ui)
        self.assertNotIn("_edit", ui)
        self.assertNotIn("goals_edit", ui)

    def test_v1_resources_do_not_expose_goal_editing_copy(self):
        for path in (EN, FR):
            source = path.read_text(encoding="utf-8")
            self.assertNotIn('name="goals_edit"', source)
            self.assertNotIn('name="goals_update"', source)

    def test_full_habits_reset_clears_behavior_and_goal_data(self):
        main = self.read("MainActivity.kt")

        self.assertIn("BehaviorRepository.clearAll(context)", main)
        self.assertIn("BehaviorGoalRepository.clearAll(context)", main)


if __name__ == "__main__":
    unittest.main()
