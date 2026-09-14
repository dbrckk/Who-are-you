from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class ProfileResetContractTest(unittest.TestCase):
    def setUp(self):
        self.store = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileStore.kt").read_text(encoding="utf-8")
        self.profile = (ROOT / "app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt").read_text(encoding="utf-8")
        self.main = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
        self.en = (ROOT / "app/src/main/res/values/strings.xml").read_text(encoding="utf-8")
        self.fr = (ROOT / "app/src/main/res/values-fr/strings.xml").read_text(encoding="utf-8")

    def test_store_exposes_bounded_profile_clear(self):
        self.assertIn("suspend fun clearLocalProfile(context: Context)", self.store)
        self.assertIn("context.profileDataStore.edit { it.clear() }", self.store)

    def test_reset_requires_confirmation(self):
        self.assertIn("AlertDialog(", self.profile)
        self.assertIn("showResetDialog = true", self.profile)
        self.assertIn("onResetLocalData()", self.profile)

    def test_main_wires_reset_to_store(self):
        self.assertIn("ProfileStore.clearLocalProfile(context)", self.main)

    def test_copy_is_localized_and_scope_is_profile_specific(self):
        for source in (self.en, self.fr):
            self.assertIn('name="reset_local_data_button"', source)
            self.assertIn('name="reset_local_data_title"', source)
            self.assertIn('name="reset_local_data_body"', source)
        self.assertIn("RESET PROFILE DATA", self.en)
        self.assertIn("RÉINITIALISER LES DONNÉES DU PROFIL", self.fr)


if __name__ == "__main__":
    unittest.main()
