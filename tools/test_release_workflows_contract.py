from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ReleaseWorkflowContractTest(unittest.TestCase):
    def setUp(self):
        self.ci = (ROOT / ".github/workflows/android-ci.yml").read_text(encoding="utf-8")
        self.internal = (ROOT / ".github/workflows/play-internal-publish.yml").read_text(encoding="utf-8")
        self.candidate = (ROOT / ".github/workflows/play-candidate.yml").read_text(encoding="utf-8")
        self.circleci = (ROOT / ".circleci/config.yml").read_text(encoding="utf-8")

    def test_ci_runs_automatically(self):
        self.assertIn("pull_request:", self.ci)
        self.assertIn("push:", self.ci)
        self.assertIn("- main", self.ci)

    def test_internal_publish_has_single_release_tail(self):
        self.assertEqual(1, self.internal.count("- name: Upload and commit Google Play edit"))
        self.assertEqual(1, self.internal.count("- name: Upload publishing receipt"))
        self.assertEqual(1, self.internal.count("- name: Remove temporary credentials"))
        self.assertNotIn("|| echo \"(none)\"\n          } | tee", self.internal)

    def test_internal_publish_verifies_bundle_and_budget(self):
        self.assertIn("jarsigner -verify -verbose -certs", self.internal)
        self.assertIn("play-internal-bundle-budget.json", self.internal)
        self.assertIn("app/build/outputs/mapping/playRelease/mapping.txt", self.internal)
        self.assertIn("grep -E '(^|/)lib/[^/]+/[^/]+\\.so$'", self.internal)

    def test_circleci_runs_real_android_quality_gates(self):
        self.assertIn("cimg/android:2026.08.1", self.circleci)
        self.assertIn("PYTHONPATH=tools python -m unittest discover", self.circleci)
        self.assertIn("gradle :app:testDebugUnitTest", self.circleci)
        self.assertIn("gradle :app:lintDebug", self.circleci)
        self.assertIn("gradle :app:assembleDebugAndroidTest", self.circleci)

    def test_candidate_and_internal_run_core_quality_gates(self):
        for workflow in (self.candidate, self.internal):
            self.assertIn(":app:testDebugUnitTest", workflow)
            self.assertIn(":app:assembleDebugAndroidTest", workflow)
            self.assertIn(":app:lintDebug", workflow)
            self.assertIn(":app:lintPlayRelease", workflow)
            self.assertIn(":baseline-profile:assembleBenchmark", workflow)
            self.assertIn("test -s app/build/outputs/mapping/playRelease/mapping.txt", workflow)
            self.assertIn("timeout-minutes: 40", workflow)

class ComposeImportContractTest(unittest.TestCase):
    def _assert_extension_import(self, token, import_line, label):
        root = ROOT / "app/src/main/java/com/whoareyou/app"
        offenders = []
        for path in root.glob("*.kt"):
            source = path.read_text(encoding="utf-8")
            if token not in source:
                continue
            if "androidx.compose.foundation.layout.*" in source:
                continue
            if import_line not in source:
                offenders.append(path.name)
        self.assertEqual([], offenders, f"Missing Compose {label} import: {offenders}")

    def test_padding_extension_has_import_when_used(self):
        self._assert_extension_import(
            ".padding(",
            "import androidx.compose.foundation.layout.padding",
            "padding",
        )

    def test_weight_extension_has_import_when_used(self):
        self._assert_extension_import(
            ".weight(",
            "import androidx.compose.foundation.layout.weight",
            "weight",
        )

    def test_height_extension_has_import_when_used(self):
        self._assert_extension_import(
            ".height(",
            "import androidx.compose.foundation.layout.height",
            "height",
        )

if __name__ == "__main__":
    unittest.main()
