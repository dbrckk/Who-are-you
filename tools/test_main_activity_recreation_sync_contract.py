from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (
    ROOT / "app/src/androidTest/java/com/whoareyou/app/MainActivityRecreationTest.kt"
).read_text(encoding="utf-8")


class MainActivityRecreationSyncContractTest(unittest.TestCase):
    def test_recreation_test_uses_v2_compose_rule(self):
        self.assertIn(
            "import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule",
            SOURCE,
        )
        self.assertNotIn(
            "import androidx.compose.ui.test.junit4.createEmptyComposeRule",
            SOURCE,
        )

    def test_recreation_test_waits_for_discover_before_scrolling(self):
        test_body = SOURCE.split(
            "fun quizProgressSurvivesActivityRecreation()",
            1,
        )[1]
        wait_index = test_body.index('waitForTag("app_screen_discover")')
        scroll_index = test_body.index('onNodeWithTag("discover_list")')
        self.assertLess(wait_index, scroll_index)


if __name__ == "__main__":
    unittest.main()
