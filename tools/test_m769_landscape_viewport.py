import struct
import subprocess
import tempfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
VALIDATOR = ROOT / ".github" / "scripts" / "validate-ui-hierarchy.py"


def _write_png_header(path: Path, width: int, height: int) -> None:
    signature = b"\x89PNG\r\n\x1a\n"
    ihdr = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    path.write_bytes(signature + struct.pack(">I", len(ihdr)) + b"IHDR" + ihdr)


def test_landscape_viewport_comes_from_rendered_screenshot() -> None:
    with tempfile.TemporaryDirectory() as tmp:
        tmp_path = Path(tmp)
        screenshot = tmp_path / "landscape.png"
        hierarchy = tmp_path / "landscape.xml"

        _write_png_header(screenshot, 640, 320)
        hierarchy.write_text(
            '<?xml version="1.0" encoding="UTF-8"?>'
            '<hierarchy rotation="1">'
            '<node index="0" text="" resource-id="" class="android.widget.FrameLayout" '
            'package="com.whoareyou.app" clickable="false" enabled="true" '
            'bounds="[0,0][640,320]" />'
            '</hierarchy>',
            encoding="utf-8",
        )

        result = subprocess.run(
            [
                "python3",
                str(VALIDATOR),
                str(hierarchy),
                "--package",
                "com.whoareyou.app",
                "--screenshot",
                str(screenshot),
                "--density",
                "160",
            ],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )

        assert result.returncode == 0, result.stdout + result.stderr
        assert "viewport=640x320px" in result.stdout
