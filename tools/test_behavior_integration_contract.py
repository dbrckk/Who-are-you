from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"


class BehaviorIntegrationContractTest(unittest.TestCase):
    def read(self, name: str) -> str:
        return (APP / name).read_text(encoding="utf-8")

    def test_profile_exposes_habits_entry(self):
        profile = self.read("ProfileScreenUi.kt")
        self.assertIn("onOpenHabits: () -> Unit", profile)
        self.assertIn("HabitsProfileEntry(onOpenHabits = onOpenHabits)", profile)

    def test_main_activity_connects_profile_to_habits(self):
        source = self.read("MainActivity.kt")
        self.assertIn("onOpenHabits = { navigate(AppNavigation.habitsDestination()) }", source)

    def test_habits_back_destination_is_profile(self):
        source = self.read("AppNavigation.kt")
        self.assertIn("AppScreen.HABITS -> AppScreen.PROFILE", source)

    def test_behavior_refreshes_on_app_resume(self):
        source = self.read("BehaviorLifecycleUi.kt")
        self.assertIn("Lifecycle.Event.ON_RESUME", source)
        self.assertIn("currentRefresh()", source)
        main = self.read("MainActivity.kt")
        self.assertIn("BehaviorRefreshOnResume(context as? ComponentActivity)", main)
        self.assertIn("BehaviorSourceActionHandler.handle(context, source, action)", main)


if __name__ == "__main__":
    unittest.main()
