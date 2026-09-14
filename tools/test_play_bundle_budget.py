import json
import tempfile
import unittest
import zipfile
from pathlib import Path

import check_play_bundle_budget as budget


class PlayBundleBudgetTest(unittest.TestCase):
    def test_inspection_reports_bundle_structure(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "sample.aab"
            with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_STORED) as archive:
                archive.writestr("base/dex/classes.dex", b"dex")
                archive.writestr("base/res/drawable/icon.xml", b"<x/>")
                archive.writestr("base/lib/arm64-v8a/libsample.so", b"so")
            report = budget.inspect_bundle(path)

        self.assertEqual(1, report["dex_file_count"])
        self.assertEqual(1, report["resource_entry_count"])
        self.assertEqual(1, report["native_library_count"])
        self.assertGreater(report["size_bytes"], 0)

    def test_internal_defaults_are_explicit_and_ordered(self):
        self.assertEqual(30, budget.DEFAULT_WARN_MIB)
        self.assertEqual(50, budget.DEFAULT_FAIL_MIB)
        self.assertLess(budget.DEFAULT_WARN_MIB, budget.DEFAULT_FAIL_MIB)


if __name__ == "__main__":
    unittest.main()
