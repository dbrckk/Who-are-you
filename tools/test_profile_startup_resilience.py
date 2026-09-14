from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class ProfileStartupResilienceTest(unittest.TestCase):
    def setUp(self):
        self.source = (
            ROOT / "app/src/main/java/com/whoareyou/app/ProfileStore.kt"
        ).read_text(encoding="utf-8")

    def test_default_profile_does_not_skip_onboarding(self):
        self.assertIn("val onboardingComplete: Boolean = false", self.source)
        self.assertNotIn("val onboardingComplete: Boolean = true", self.source)

    def test_profile_observation_recovers_with_empty_preferences(self):
        self.assertIn("import kotlinx.coroutines.flow.catch", self.source)
        self.assertIn(".catch { error ->", self.source)\n        self.assertIn("if (error is IOException)", self.source)\n        self.assertIn("emit(emptyPreferences())", self.source)\n        self.assertIn("throw error", self.source)
        self.assertIn(".map(::decodeProfile)", self.source)


if __name__ == "__main__":
    unittest.main()
