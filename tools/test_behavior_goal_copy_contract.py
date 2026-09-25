from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
EN = ROOT / "app/src/main/res/values/strings.xml"
FR = ROOT / "app/src/main/res/values-fr/strings.xml"

REQUIRED = {
    "goals_title",
    "goals_user_defined",
    "goals_create",
    "goals_metric_steps",
    "goals_metric_screen_time",
    "goals_metric_evening_usage",
    "goals_metric_app_usage",
    "goals_status_active",
    "goals_status_paused",
    "goals_status_completed",
    "goals_your_target",
    "goals_observed_days",
    "goals_target_met_days",
    "goals_missing_evidence",
    "goals_days_remaining",
    "goals_pause",
    "goals_resume",
    "goals_delete",
}

FORBIDDEN = {
    "healthy", "unhealthy", "addict", "diagnos", "personality",
    "depress", "anxiety", "lazy", "success", "failure",
    "sain", "malsain", "dépend", "diagnost", "personnalité",
    "dépress", "anxi", "paresse", "succès", "échec",
}


def strings(path: Path):
    source = path.read_text(encoding="utf-8")
    return dict(re.findall(r'<string name="([^"]+)">(.*?)</string>', source, flags=re.DOTALL))


class BehaviorGoalCopyContractTest(unittest.TestCase):
    def test_goal_copy_has_en_fr_parity(self):
        en = strings(EN)
        fr = strings(FR)
        self.assertTrue(REQUIRED.issubset(en.keys()), REQUIRED - en.keys())
        self.assertTrue(REQUIRED.issubset(fr.keys()), REQUIRED - fr.keys())

    def test_goal_copy_states_targets_as_user_defined(self):
        en = strings(EN)
        fr = strings(FR)
        self.assertIn("you", en["goals_user_defined"].lower())
        self.assertIn("your", en["goals_your_target"].lower())
        self.assertIn("toi", fr["goals_user_defined"].lower())
        self.assertIn("ton", fr["goals_your_target"].lower())

    def test_goal_copy_stays_non_clinical_and_non_moralizing(self):
        for path in (EN, FR):
            values = strings(path)
            goal_copy = " ".join(
                value for key, value in values.items() if key.startswith("goals_")
            ).lower()
            for word in FORBIDDEN:
                self.assertNotIn(word, goal_copy, f"{word} in {path}")


if __name__ == "__main__":
    unittest.main()
