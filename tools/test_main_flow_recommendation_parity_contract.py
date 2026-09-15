from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
TEST_SOURCE = (
    ROOT / "app/src/androidTest/java/com/whoareyou/app/MainFlowE2eTest.kt"
).read_text(encoding="utf-8")


class MainFlowRecommendationParityContractTest(unittest.TestCase):
    def test_e2e_recommendation_uses_same_profile_coverage_as_discover_ui(self):
        self.assertEqual(
            2,
            TEST_SOURCE.count("coverage = summary.coverage"),
            "MainFlowE2eTest must calculate the expected recommended quiz with the same coverage-aware path as PersonalizedDiscoverDashboard",
        )


if __name__ == "__main__":
    unittest.main()
