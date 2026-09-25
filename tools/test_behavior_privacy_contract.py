from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"


class BehaviorPrivacyContractTest(unittest.TestCase):
    def read(self, name: str) -> str:
        return (APP / name).read_text(encoding="utf-8")

    def test_behavior_measurements_do_not_enter_telemetry(self):
        source = self.read("AppEvents.kt")
        forbidden = [
            "BehaviorSnapshot",
            "DailyBehaviorAggregate",
            "ActivityDay",
            "totalForegroundMillis",
            "topApps",
            "launchesOrSessions",
            "daypartUsage",
        ]
        for token in forbidden:
            self.assertNotIn(token, source, token)

    def test_behavior_measurements_do_not_enter_profile_share(self):
        source = self.read("GlobalProfileShare.kt")
        forbidden = [
            "BehaviorSnapshot",
            "DailyBehaviorAggregate",
            "ActivityDay",
            "totalForegroundMillis",
            "topApps",
            "launchesOrSessions",
            "daypartUsage",
        ]
        for token in forbidden:
            self.assertNotIn(token, source, token)

    def test_behavior_measurements_do_not_enter_ad_configuration(self):
        source = self.read("AdManager.kt")
        forbidden = [
            "BehaviorSnapshot",
            "DailyBehaviorAggregate",
            "ActivityDay",
            "BehaviorRepository",
            "totalForegroundMillis",
            "topApps",
            "launchesOrSessions",
            "daypartUsage",
        ]
        for token in forbidden:
            self.assertNotIn(token, source, token)

    def test_personal_model_engine_has_no_behavior_dependency(self):
        source = self.read("PersonalModelEngine.kt")
        forbidden = [
            "BehaviorSnapshot",
            "DailyBehaviorAggregate",
            "BehaviorRepository",
            "ActivityDay",
            "AppUsageAggregate",
        ]
        for token in forbidden:
            self.assertNotIn(token, source, token)

    def test_behavior_domain_does_not_write_profile_store_or_personal_model(self):
        behavior_files = [
            "BehaviorInsightEngine.kt",
            "BehaviorModels.kt",
            "BehaviorRefreshCoordinator.kt",
            "BehaviorRepository.kt",
            "BehaviorStore.kt",
            "ActivityCollector.kt",
            "AppUsageCollector.kt",
            "UsageAccess.kt",
        ]
        for name in behavior_files:
            source = self.read(name)
            self.assertNotIn("ProfileStore", source, name)
            self.assertNotIn("PersonalModelEngine", source, name)
            self.assertNotIn("PersonalCertainty", source, name)


if __name__ == "__main__":
    unittest.main()
