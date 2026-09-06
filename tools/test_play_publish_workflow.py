import pathlib
import unittest


class PlayPublishWorkflowContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.workflow = pathlib.Path('.github/workflows/play-internal-publish.yml').read_text(encoding='utf-8')

    def test_manual_only_and_read_only(self):
        self.assertIn('workflow_dispatch:', self.workflow)
        self.assertNotIn('\n  push:', self.workflow)
        self.assertIn('contents: read', self.workflow)

    def test_publish_is_opt_in(self):
        self.assertIn('publish_to_play:', self.workflow)
        self.assertIn('default: false', self.workflow)
        self.assertIn('if: ${{ inputs.publish_to_play == true }}', self.workflow)

    def test_internal_track_publisher_and_service_account_secret(self):
        self.assertIn('tools/play_publisher.py', self.workflow)
        self.assertIn('GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_B64', self.workflow)
        self.assertIn('environment: play-internal', self.workflow)

    def test_build_uses_strict_play_release_and_signature_verification(self):
        self.assertIn(':app:bundlePlayRelease', self.workflow)
        self.assertIn('jarsigner -verify -verbose -certs', self.workflow)
        self.assertIn('play-internal-upload.sha256', self.workflow)

    def test_actual_upload_commits_edit_only_in_publish_job(self):
        self.assertIn('--commit', self.workflow)
        self.assertIn('--status "$RELEASE_STATUS"', self.workflow)

    def test_temporary_credentials_are_cleaned(self):
        self.assertIn('if: always()', self.workflow)
        self.assertIn('rm -f "$RUNNER_TEMP/who-are-you-upload.jks"', self.workflow)
        self.assertIn('rm -f "$RUNNER_TEMP/google-play-service-account.json"', self.workflow)


if __name__ == '__main__':
    unittest.main()
