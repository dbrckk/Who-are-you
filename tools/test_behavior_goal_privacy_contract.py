from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
ANDROID_NS = "http://schemas.android.com/apk/res/android"

GOAL_TOKENS = [
    "BehaviorGoal",
    "BehaviorGoalMetric",
    "BehaviorGoalProgress",
    "BehaviorGoalRepository",
    "BehaviorGoalCreateRequest",
]

GOAL_FILES = [
    "BehaviorGoalEngine.kt",
    "BehaviorGoalRepository.kt",
    "BehaviorGoalStore.kt",
    "BehaviorGoalUiModel.kt",
    "BehaviorGoalPresentation.kt",
    "BehaviorGoalIntegrationUi.kt",
    "BehaviorGoalsUi.kt",
]


class BehaviorGoalPrivacyContractTest(unittest.TestCase):
    def read(self, name: str) -> str:
        return (APP / name).read_text(encoding="utf-8")

    def test_goal_data_does_not_enter_telemetry_share_ads_or_personal_model(self):
        for name in (
            "AppEvents.kt",
            "GlobalProfileShare.kt",
            "AdManager.kt",
            "PersonalModelEngine.kt",
        ):
            source = self.read(name)
            for token in GOAL_TOKENS:
                self.assertNotIn(token, source, f"{token} in {name}")

    def test_goal_domain_does_not_write_profile_or_external_services(self):
        forbidden = [
            "ProfileStore",
            "PersonalModelEngine",
            "PersonalCertainty",
            "AppEvents",
            "AdManager",
            "GlobalProfileShare",
            "HttpURLConnection",
            "Retrofit",
            "OkHttp",
        ]
        for name in GOAL_FILES:
            source = self.read(name)
            for token in forbidden:
                self.assertNotIn(token, source, f"{token} in {name}")

    def test_goal_feature_declares_no_new_android_permission(self):
        root = ET.fromstring(MANIFEST.read_text(encoding="utf-8"))
        declared = {
            permission.attrib[f"{{{ANDROID_NS}}}name"]
            for permission in root.findall("uses-permission")
        }
        self.assertEqual(
            {
                "android.permission.INTERNET",
                "android.permission.PACKAGE_USAGE_STATS",
                "android.permission.health.READ_STEPS",
            },
            declared,
        )

        for name in GOAL_FILES:
            source = self.read(name)
            self.assertNotIn("Manifest.permission", source, name)
            self.assertNotIn("PermissionController", source, name)


if __name__ == "__main__":
    unittest.main()
