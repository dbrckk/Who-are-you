from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class PublicResultAndStorePositioningTest(unittest.TestCase):
    def setUp(self):
        self.result = (ROOT / "app/src/main/java/com/whoareyou/app/ResultScreenUi.kt").read_text(encoding="utf-8")
        self.next_engine = (ROOT / "app/src/main/java/com/whoareyou/app/ResultNextExploration.kt").read_text(encoding="utf-8")
        self.en_listing = (ROOT / "docs/store-listing-en.md").read_text(encoding="utf-8")
        self.fr_listing = (ROOT / "docs/store-listing-fr.md").read_text(encoding="utf-8")
        self.copy_sheet = (ROOT / "docs/play-console-copy-paste.md").read_text(encoding="utf-8")

    def test_done_is_primary_before_optional_social_actions(self):
        done_index = self.result.index('testTag("result_done")')
        share_index = self.result.index("R.string.share_my_result")
        compare_index = self.result.index("R.string.compare_with_friend")
        self.assertLess(done_index, share_index)
        self.assertLess(done_index, compare_index)
        self.assertIn("R.string.result_optional_actions", self.result)

    def test_completed_catalog_does_not_force_retake_recommendation(self):
        self.assertNotIn(
            "ResultNextExploration(it, ResultNextReason.RETAKE_FACET)",
            self.next_engine,
        )

    def test_store_positioning_leads_with_self_reflection(self):
        self.assertIn("self-reflection profile", self.en_listing)
        self.assertIn("profil visuel dédié à l’introspection", self.fr_listing)
        assets = (ROOT / "docs/store-assets-spec.md").read_text(encoding="utf-8")
        self.assertIn("social comparison or retention only after the core self-reflection story", assets)

    def test_play_console_sheet_is_version_agnostic(self):
        self.assertIn("authoritative version name/code comes from `app/build.gradle.kts`", self.copy_sheet)
        self.assertNotIn("0.1.0 (1)", self.copy_sheet)


if __name__ == "__main__":
    unittest.main()
