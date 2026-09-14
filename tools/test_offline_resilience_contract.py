from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class OfflineResilienceContractTest(unittest.TestCase):
    def setUp(self):
        self.telemetry = (ROOT / 'app/src/main/java/com/whoareyou/app/Telemetry.kt').read_text(encoding='utf-8')
        self.events = (ROOT / 'app/src/main/java/com/whoareyou/app/AppEvents.kt').read_text(encoding='utf-8')
        self.share = (ROOT / 'app/src/main/java/com/whoareyou/app/ShareSafety.kt').read_text(encoding='utf-8')

    def test_http_telemetry_queue_is_bounded(self):
        self.assertIn('ArrayBlockingQueue(64)', self.telemetry)
        self.assertIn('ThreadPoolExecutor.DiscardOldestPolicy()', self.telemetry)
        self.assertIn('connectTimeout = 2500', self.telemetry)
        self.assertIn('readTimeout = 2500', self.telemetry)

    def test_disabled_production_telemetry_is_noop(self):
        self.assertIn('internal object NoOpEventSink', self.telemetry)
        self.assertIn('if (BuildConfig.DEBUG) LogEventSink else NoOpEventSink', self.events)

    def test_share_launcher_handles_non_activity_contexts_and_failures(self):
        self.assertIn('context !is Activity', self.share)
        self.assertIn('Intent.FLAG_ACTIVITY_NEW_TASK', self.share)
        self.assertIn('ShareSafety', self.share)
        self.assertIn('R.string.share_failed', self.share)

if __name__ == '__main__':
    unittest.main()
