import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class PlayCandidateWorkflowTest(unittest.TestCase):
    def setUp(self):
        self.contract = json.loads((ROOT / 'docs/m32-play-candidate-contract.json').read_text(encoding='utf-8'))
        self.workflow = (ROOT / self.contract['workflow']).read_text(encoding='utf-8')

    def test_manual_only_and_read_only(self):
        self.assertIn('workflow_dispatch:', self.workflow)
        self.assertNotIn('\n  push:', self.workflow)
        self.assertNotIn('\n  pull_request:', self.workflow)
        self.assertIn('contents: read', self.workflow)
        self.assertIn('environment: play-internal', self.workflow)

    def test_required_secrets_are_referenced(self):
        for secret in self.contract['requiredSecrets']:
            self.assertIn(f'secrets.{secret}', self.workflow)

    def test_keystore_is_temporary_and_cleaned(self):
        self.assertIn('$RUNNER_TEMP/who-are-you-upload.jks', self.workflow)
        self.assertIn('if: always()', self.workflow)
        self.assertIn('rm -f "$RUNNER_TEMP/who-are-you-upload.jks"', self.workflow)

    def test_signed_candidate_is_verified_and_uploaded(self):
        self.assertIn(':app:bundlePlayRelease', self.workflow)
        self.assertIn('jarsigner -verify -verbose -certs', self.workflow)
        self.assertIn('sha256sum', self.workflow)
        gradle = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
        version_name = re.search(r'versionName\s*=\s*"([^"]+)"', gradle).group(1)
        version_code = re.search(r'versionCode\s*=\s*(\d+)', gradle).group(1)
        self.assertIn(f'who-are-you-play-candidate-{version_name}-{version_code}', self.workflow)
        self.assertIn(f'who-are-you-play-{version_name}-{version_code}.aab', self.workflow)

    def test_workflow_does_not_upload_to_play(self):
        self.assertFalse(self.contract['playUploadAutomated'])
        lowered = self.workflow.lower()
        self.assertNotIn('playdeveloperreporting', lowered)
        self.assertNotIn('google-github-actions', lowered)


if __name__ == '__main__':
    unittest.main()
