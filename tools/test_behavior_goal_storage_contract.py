from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"


class BehaviorGoalStorageContractTest(unittest.TestCase):
    def test_goal_repository_uses_separate_local_datastore(self):
        path = APP / "BehaviorGoalRepository.kt"
        self.assertTrue(path.exists(), "BehaviorGoalRepository.kt must exist")
        source = path.read_text(encoding="utf-8")

        self.assertIn('name = "who_are_you_behavior_goals"', source)
        self.assertIn("object BehaviorGoalRepository", source)
        self.assertIn("fun observe(context: Context): Flow<List<BehaviorGoal>>", source)
        self.assertIn("suspend fun upsert(context: Context, goal: BehaviorGoal)", source)
        self.assertIn("suspend fun setPaused(context: Context, id: String, paused: Boolean)", source)
        self.assertIn("suspend fun remove(context: Context, id: String)", source)
        self.assertIn("suspend fun clearAll(context: Context)", source)

    def test_goal_repository_does_not_write_profile_or_behavior_history(self):
        source = (APP / "BehaviorGoalRepository.kt").read_text(encoding="utf-8")

        self.assertNotIn("ProfileStore", source)
        self.assertNotIn("BehaviorRepository", source)
        self.assertNotIn("AppEvents", source)
        self.assertNotIn("Http", source)
        self.assertNotIn("AdManager", source)


if __name__ == "__main__":
    unittest.main()
