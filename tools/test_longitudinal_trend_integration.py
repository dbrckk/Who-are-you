from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class LongitudinalTrendIntegrationContractTest(unittest.TestCase):
    def setUp(self):
        self.store = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileStore.kt").read_text(encoding="utf-8")
        self.global_profile = (ROOT / "app/src/main/java/com/whoareyou/app/GlobalProfile.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")
        self.ui = (ROOT / "app/src/main/java/com/whoareyou/app/LongitudinalTrendUi.kt").read_text(encoding="utf-8")
        self.engine = (ROOT / "app/src/main/java/com/whoareyou/app/LongitudinalTrend.kt").read_text(encoding="utf-8")

    def test_dated_history_is_persisted(self):
        self.assertIn("timedScoreHistory: Map<String, List<TimedScore>>", self.store)
        self.assertIn("timed_score_history_v1", self.store)
        self.assertIn("LocalDate.now().toEpochDay()", self.store)

    def test_global_profile_carries_longitudinal_trends(self):
        self.assertIn("val longitudinalTrends: List<LongitudinalTrend>", self.global_profile)
        self.assertIn("LongitudinalTrendEngine.build", self.global_profile)

    def test_profile_renders_accessible_sparklines(self):
        self.assertIn("LongitudinalTrendsCard(", self.profile)
        self.assertIn(".drawWithCache {", self.ui)
        self.assertIn("contentDescription = description", self.ui)

    def test_engine_distinguishes_outlier_volatility_and_direction(self):
        for kind in ("RISING", "FALLING", "VOLATILE", "OUTLIER", "STABLE"):
            self.assertIn(kind, self.engine)

if __name__ == "__main__":
    unittest.main()
