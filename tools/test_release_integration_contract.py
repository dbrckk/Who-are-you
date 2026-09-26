from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
ANDROID_NS = "http://schemas.android.com/apk/res/android"


class ReleaseIntegrationContractTest(unittest.TestCase):
    def read(self, name: str) -> str:
        return (APP / name).read_text(encoding="utf-8")

    def test_fresh_behavior_state_is_disabled_until_user_action(self):
        main = self.read("MainActivity.kt")
        self.assertIn(
            "sourceStates = BehaviorSource.entries.associateWith { BehaviorSourceState.DISABLED }",
            main,
        )
        self.assertIn(
            "BehaviorSourceEffect.REQUEST_ACTIVITY_PERMISSION -> activityPermissionLauncher.launch",
            main,
        )
        self.assertIn(
            "BehaviorSourceEffect.OPEN_USAGE_ACCESS -> usageAccessLauncher.launch",
            main,
        )

    def test_permission_launches_are_confined_to_user_source_actions(self):
        main = self.read("MainActivity.kt")
        self.assertEqual(1, main.count("activityPermissionLauncher.launch("))
        self.assertEqual(1, main.count("usageAccessLauncher.launch("))
        self.assertIn("onSourceAction = { source, action ->", main)

    def test_profile_and_habits_resets_have_separate_scopes(self):
        main = self.read("MainActivity.kt")

        profile_reset = (
            "onResetLocalData = { scope.launch { "
            "ProfileStore.clearLocalProfile(context); navigate(AppScreen.DISCOVER) } }"
        )
        habits_reset = (
            "onDeleteAll = { scope.launch { BehaviorRepository.clearAll(context); "
            "BehaviorGoalRepository.clearAll(context) } }"
        )

        self.assertIn(profile_reset, main)
        self.assertIn(habits_reset, main)
        self.assertNotIn(
            "ProfileStore.clearLocalProfile(context); BehaviorRepository.clearAll(context)",
            main,
        )

    def test_goal_progress_is_recomputed_not_persisted(self):
        repo = self.read("BehaviorGoalRepository.kt")
        presentation = self.read("BehaviorGoalPresentation.kt")
        integration = self.read("BehaviorGoalIntegrationUi.kt")

        self.assertNotIn("BehaviorGoalProgress", repo)
        self.assertNotIn("BehaviorSnapshot", repo)
        self.assertIn("days = snapshot.last30Days", presentation)
        self.assertIn("BehaviorGoalRepository.observe", integration)
        self.assertIn("BehaviorGoalPresentation.build", integration)

    def test_release_hardening_adds_no_permission(self):
        root = ET.fromstring(MANIFEST.read_text(encoding="utf-8"))
        declared = {
            node.attrib[f"{{{ANDROID_NS}}}name"]
            for node in root.findall("uses-permission")
        }
        self.assertEqual(
            {
                "android.permission.INTERNET",
                "android.permission.PACKAGE_USAGE_STATS",
                "android.permission.health.READ_STEPS",
            },
            declared,
        )

    def test_behavior_and_goal_stores_remain_separate(self):
        behavior = self.read("BehaviorRepository.kt")
        goals = self.read("BehaviorGoalRepository.kt")
        self.assertIn('name = "who_are_you_behavior"', behavior)
        self.assertIn('name = "who_are_you_behavior_goals"', goals)
        self.assertNotIn("BehaviorGoalRepository", behavior)
        self.assertNotIn("ProfileStore", goals)


if __name__ == "__main__":
    unittest.main()
