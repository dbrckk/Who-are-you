import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
APP_GRADLE = ROOT / "app" / "build.gradle.kts"
MAIN_RC = ROOT / ".github" / "workflows" / "m56-main-rc.yml"
ANDROID_TEST = ROOT / "app" / "src" / "androidTest" / "java" / "com" / "whoareyou" / "app"


class M47UiTestWiringTest(unittest.TestCase):
    def test_compose_ui_test_dependencies_and_runner_stay_enabled(self):
        source = APP_GRADLE.read_text(encoding="utf-8")
        self.assertIn('testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"', source)
        self.assertIn('androidTestImplementation("androidx.compose.ui:ui-test-junit4")', source)
        self.assertIn('debugImplementation("androidx.compose.ui:ui-test-manifest")', source)

    def test_github_actions_compiles_instrumentation_test_apk(self):
        source = MAIN_RC.read_text(encoding="utf-8")
        self.assertIn("gradle :app:assembleDebugAndroidTest --stacktrace", source)

    def test_critical_compose_ui_tests_exist(self):
        for filename in (
            "QuizScreenUiTest.kt",
            "AppShellUiTest.kt",
            "ResultScreenUiTest.kt",
        ):
            self.assertTrue((ANDROID_TEST / filename).is_file(), f"Missing Compose UI test: {filename}")


if __name__ == "__main__":
    unittest.main()
