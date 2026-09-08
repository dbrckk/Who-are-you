from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class M55ReleaseAutomationTest(unittest.TestCase):
    def test_publisher_uses_current_internal_track_and_production_guard(self):
        text = (ROOT / "tools/play_publisher.py").read_text(encoding="utf-8")
        self.assertIn('INTERNAL_TRACK = "qa"', text)
        self.assertIn('PRODUCTION_TRACK = "production"', text)
        self.assertIn('Production publishing requires explicit confirmation', text)
        self.assertIn('userFraction', text)

    def test_promotion_helper_exists_without_bundle_upload(self):
        text = (ROOT / "tools/play_promoter.py").read_text(encoding="utf-8")
        self.assertIn('Promote an already-uploaded Play version', text)
        self.assertIn('tracks/{track}', text)
        self.assertNotIn('/bundles', text)

    def test_promotion_workflow_is_manual_and_safe_by_default(self):
        text = (ROOT / ".github/workflows/play-promote.yml").read_text(encoding="utf-8")
        self.assertIn('workflow_dispatch:', text)
        self.assertNotIn('\n  push:', text)
        self.assertIn('confirm_production:', text)
        self.assertIn('default: false', text)
        self.assertIn('commit_edit:', text)
        self.assertIn('python tools/play_publisher.py', text)
        self.assertIn('python tools/play_promoter.py', text)
        self.assertIn('--confirm-production', text)
        self.assertIn('if: always()', text)

    def test_public_preflight_remains_available(self):
        text = (ROOT / "tools/play_preflight.py").read_text(encoding="utf-8")
        self.assertIn('repository_ready', text)
        self.assertIn('public_release_ready', text)
        self.assertIn('external_inputs', text)


if __name__ == "__main__":
    unittest.main()
