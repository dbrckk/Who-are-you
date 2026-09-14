import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github" / "scripts" / "validate-ui-hierarchy.py"


def write_xml(path: Path, bounds: str, clickable: bool = True) -> None:
    path.write_text(
        f'''<?xml version="1.0" encoding="UTF-8"?>
<hierarchy rotation="0">
  <node index="0" text="" resource-id="com.whoareyou.app:id/root"
        class="android.view.View" package="com.whoareyou.app"
        content-desc="" clickable="false" enabled="true"
        bounds="[0,0][720,1600]">
    <node index="0" text="Action" resource-id="com.whoareyou.app:id/action"
          class="android.widget.Button" package="com.whoareyou.app"
          content-desc="Action" clickable="{str(clickable).lower()}" enabled="true"
          bounds="{bounds}" />
  </node>
</hierarchy>
''',
        encoding="utf-8",
    )


class UiHierarchyValidatorTests(unittest.TestCase):
    def run_validator(self, xml: Path):
        return subprocess.run(
            [
                sys.executable,
                str(SCRIPT),
                str(xml),
                "--package",
                "com.whoareyou.app",
                "--size",
                "720x1600",
                "--density",
                "320",
            ],
            cwd=ROOT,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            check=False,
        )

    def test_accepts_valid_48dp_plus_target(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "ui.xml"
            # 120px at 320dpi = 60dp.
            write_xml(path, "[20,20][140,140]")
            result = self.run_validator(path)
            self.assertEqual(result.returncode, 0, result.stdout)

    def test_rejects_undersized_clickable(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "ui.xml"
            # 80px at 320dpi = 40dp.
            write_xml(path, "[20,20][100,100]")
            result = self.run_validator(path)
            self.assertNotEqual(result.returncode, 0, result.stdout)
            self.assertIn("below 48dp", result.stdout)

    def test_rejects_outside_viewport(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "ui.xml"
            write_xml(path, "[650,1500][760,1620]", clickable=False)
            result = self.run_validator(path)
            self.assertNotEqual(result.returncode, 0, result.stdout)
            self.assertIn("outside viewport", result.stdout)

    def test_ignores_other_packages(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "ui.xml"
            path.write_text(
                '''<?xml version="1.0" encoding="UTF-8"?>
<hierarchy rotation="0">
  <node index="0" text="System" resource-id="android:id/system"
        class="android.view.View" package="android"
        content-desc="" clickable="true" enabled="true"
        bounds="[0,0][20,20]" />
</hierarchy>
''',
                encoding="utf-8",
            )
            result = self.run_validator(path)
            self.assertEqual(result.returncode, 0, result.stdout)


if __name__ == "__main__":
    unittest.main()
