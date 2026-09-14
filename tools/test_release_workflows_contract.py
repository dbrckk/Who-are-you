from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]

class ReleaseWorkflowContractTest(unittest.TestCase):
    def setUp(self):
        self.ci = (ROOT / ".github/workflows/android-ci.yml").read_text(encoding="utf-8")
        self.internal = (ROOT / ".github/workflows/play-internal-publish.yml").read_text(encoding="utf-8")
        self.candidate = (ROOT / ".github/workflows/play-candidate.yml").read_text(encoding="utf-8")

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

    def test_candidate_and_internal_run_core_quality_gates(self):
        for workflow in (self.candidate, self.internal):
            self.assertIn(":app:testDebugUnitTest", workflow)
            self.assertIn(":app:assembleDebugAndroidTest", workflow)
            self.assertIn(":app:lintDebug", workflow)
            self.assertIn(":baseline-profile:assembleBenchmark", workflow)

class ComposeImportContractTest(unittest.TestCase):
    def test_padding_extension_has_import_when_used(self):
        root = ROOT / "app/src/main/java/com/whoareyou/app"
        offenders = []
        for path in root.glob("*.kt"):
            source = path.read_text(encoding="utf-8")
            if ".padding(" not in source:
                continue
            if "androidx.compose.foundation.layout.*" in source:
                continue
            if "import androidx.compose.foundation.layout.padding" not in source:
                offenders.append(path.name)
        self.assertEqual([], offenders, f"Missing Compose padding import: {offenders}")

if __name__ == "__main__":
    unittest.main()
