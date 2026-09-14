from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class PlayPublishPipelineContractTest(unittest.TestCase):
    def setUp(self):
        self.internal = (ROOT / '.github/workflows/play-internal-publish.yml').read_text(encoding='utf-8')
        self.promote = (ROOT / '.github/workflows/play-promote.yml').read_text(encoding='utf-8')
        self.proguard = (ROOT / 'app/proguard-rules.pro').read_text(encoding='utf-8')

    def test_internal_publish_runs_quality_gates_before_bundle(self):
        build = self.internal.index('Build signed production Play bundle')
        for step in ('Python quality tests', 'JVM unit tests', 'Compile instrumentation tests', 'Android lint'):
            self.assertLess(self.internal.index(step), build)

    def test_internal_publish_uses_dynamic_versioned_receipt(self):
        self.assertIn('artifact_name=who-are-you-play-internal-publish-${VERSION_NAME}-${VERSION_CODE}', self.internal)
        self.assertNotIn('who-are-you-play-internal-publish-0.2.2-4', self.internal)

    def test_internal_publish_uses_optional_key_password_fallback(self):
        self.assertIn('secrets.WHO_ARE_YOU_UPLOAD_KEY_PASSWORD', self.internal)
        self.assertIn('UPLOAD_KEY_PASSWORD="${UPLOAD_KEY_PASSWORD:-$UPLOAD_KEYSTORE_PASSWORD}"', self.internal)

    def test_promotion_receipt_is_not_pinned_to_old_version(self):
        self.assertNotIn('0.1.0-1', self.promote)
        self.assertIn('github.run_number', self.promote)

    def test_project_proguard_rules_do_not_keep_unused_room_or_workmanager(self):
        self.assertNotIn('androidx.work', self.proguard)
        self.assertNotIn('androidx.room', self.proguard)

if __name__ == '__main__':
    unittest.main()
