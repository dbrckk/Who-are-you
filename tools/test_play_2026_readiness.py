from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]

class Play2026ReadinessContractTest(unittest.TestCase):
    def setUp(self):
        self.gradle = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
        self.workflow = (ROOT / '.github/workflows/play-candidate.yml').read_text(encoding='utf-8')

    def test_target_sdk_meets_august_2026_play_requirement(self):
        match = re.search(r'targetSdk\s*=\s*(\d+)', self.gradle)
        self.assertIsNotNone(match)
        self.assertGreaterEqual(int(match.group(1)), 36)

    def test_compile_sdk_is_not_below_target_sdk(self):
        compile_sdk = int(re.search(r'compileSdk\s*=\s*(\d+)', self.gradle).group(1))
        target_sdk = int(re.search(r'targetSdk\s*=\s*(\d+)', self.gradle).group(1))
        self.assertGreaterEqual(compile_sdk, target_sdk)

    def test_play_candidate_runs_quality_preflight_before_signing(self):
        build_index = self.workflow.index('Build signed Play candidate')
        for step in ('Python quality tests', 'JVM unit tests', 'Compile instrumentation tests', 'Android lint'):
            self.assertLess(self.workflow.index(step), build_index)

    def test_play_candidate_installs_declared_android_sdk(self):
        self.assertIn('platforms;android-37.0', self.workflow)
        self.assertIn('build-tools;37.0.0', self.workflow)

    def test_play_artifact_reports_native_libraries(self):
        self.assertIn('play-native-libraries.txt', self.workflow)
        self.assertIn("grep -E '(^|/)lib/", self.workflow)

if __name__ == '__main__':
    unittest.main()
