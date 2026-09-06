import json
import pathlib
import unittest


class M33ReadinessTest(unittest.TestCase):
    def test_manifest_matches_locked_release(self):
        data = json.loads(pathlib.Path('docs/m33-readiness.json').read_text(encoding='utf-8'))
        self.assertEqual(data['milestone'], 'M33')
        self.assertEqual(data['applicationId'], 'com.whoareyou.app')
        self.assertEqual(data['track'], 'internal')
        self.assertEqual(data['versionCode'], 1)
        self.assertEqual(data['versionName'], '0.1.0')
        self.assertFalse(data['publishDefault'])
        self.assertEqual(data['releaseStatuses'], ['draft', 'completed'])
        self.assertEqual(data['environment'], 'play-internal')
        self.assertEqual(data['serviceAccountSecret'], 'GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_B64')
        self.assertEqual(
            data['editFlow'],
            ['insert', 'bundle_upload', 'track_update', 'validate', 'commit'],
        )


if __name__ == '__main__':
    unittest.main()
