from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class PerformanceReleaseGateContractTest(unittest.TestCase):
    def setUp(self):
        self.root_gradle = (ROOT / 'build.gradle.kts').read_text(encoding='utf-8')
        self.settings = (ROOT / 'settings.gradle.kts').read_text(encoding='utf-8')
        self.app_gradle = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
        self.baseline_gradle = (ROOT / 'baseline-profile/build.gradle.kts').read_text(encoding='utf-8')
        self.generator = (ROOT / 'baseline-profile/src/main/java/com/whoareyou/app/baselineprofile/BaselineProfileGenerator.kt').read_text(encoding='utf-8')
        self.benchmark = (ROOT / 'baseline-profile/src/main/java/com/whoareyou/app/baselineprofile/StartupBenchmark.kt').read_text(encoding='utf-8')
        self.candidate = (ROOT / '.github/workflows/play-candidate.yml').read_text(encoding='utf-8')
        self.internal = (ROOT / '.github/workflows/play-internal-publish.yml').read_text(encoding='utf-8')
        self.promote = (ROOT / '.github/workflows/play-promote.yml').read_text(encoding='utf-8')

    def test_baseline_profile_toolchain_is_wired(self):
        self.assertIn('id("androidx.baselineprofile") version "1.5.0"', self.root_gradle)
        self.assertIn('":baseline-profile"', self.settings)
        self.assertIn('baselineProfile(project(":baseline-profile"))', self.app_gradle)
        self.assertIn('androidx.profileinstaller:profileinstaller:1.4.1', self.app_gradle)
        self.assertIn('targetProjectPath = ":app"', self.baseline_gradle)

    def test_benchmark_covers_startup_and_requires_baseline_profile(self):
        self.assertIn('BaselineProfileRule()', self.generator)
        self.assertIn('By.res("onboarding_start")', self.generator)
        self.assertIn('By.res("app_screen_discover")', self.generator)
        self.assertIn('StartupTimingMetric()', self.benchmark)
        self.assertIn('StartupMode.COLD', self.benchmark)
        self.assertIn('StartupMode.WARM', self.benchmark)
        self.assertIn('BaselineProfileMode.Require', self.benchmark)

    def test_play_preflight_compiles_benchmark_variants(self):
        expected = "gradle :app:assembleBenchmark :baseline-profile:assembleBenchmark --stacktrace"
        self.assertIn(expected, self.candidate)
        self.assertIn(expected, self.internal)

    def test_play_artifacts_include_bundle_budget(self):
        self.assertIn('play-bundle-budget.json', self.candidate)
        self.assertIn('play-internal-bundle-budget.json', self.internal)

    def test_production_promotion_requires_vitals_and_prelaunch_review(self):
        self.assertIn('confirm_vitals_review', self.promote)
        self.assertIn('confirm_prelaunch_report', self.promote)
        self.assertIn('Production promotion requires Android Vitals review confirmation', self.promote)
        self.assertIn('Production promotion requires Play pre-launch report confirmation', self.promote)

if __name__ == '__main__':
    unittest.main()
