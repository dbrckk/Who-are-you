import os
import random
import struct
import subprocess
import sys
import tempfile
import unittest
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github" / "scripts" / "validate-screenshot.py"


def write_rgba_png(path: Path, width: int, height: int, uniform: bool = False) -> None:
    rows = bytearray()
    rng = random.Random(1337)
    for y in range(height):
        rows.append(0)  # filter: None
        for x in range(width):
            if uniform:
                r = g = b = 12
            else:
                r = (x * 17 + y * 3 + rng.randrange(0, 64)) & 0xFF
                g = (x * 5 + y * 13 + rng.randrange(0, 64)) & 0xFF
                b = (x * 11 + y * 7 + rng.randrange(0, 64)) & 0xFF
            rows.extend((r, g, b, 255))

    def chunk(kind: bytes, data: bytes) -> bytes:
        payload = kind + data
        return (
            struct.pack(">I", len(data))
            + payload
            + struct.pack(">I", zlib.crc32(payload) & 0xFFFFFFFF)
        )

    png = bytearray(b"\x89PNG\r\n\x1a\n")
    png.extend(chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)))
    png.extend(chunk(b"IDAT", zlib.compress(bytes(rows), 6)))
    png.extend(chunk(b"IEND", b""))
    path.write_bytes(png)


class ScreenshotValidatorTests(unittest.TestCase):
    def run_validator(self, png: Path, expected: str):
        return subprocess.run(
            [sys.executable, str(SCRIPT), str(png), "--size", expected],
            cwd=ROOT,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            check=False,
        )

    def test_accepts_non_uniform_screenshot(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "screen.png"
            write_rgba_png(path, 512, 768, uniform=False)
            result = self.run_validator(path, "512x768")
            self.assertEqual(result.returncode, 0, result.stdout)
            self.assertIn("Screenshot integrity OK", result.stdout)

    def test_accepts_rotated_dimensions(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "screen.png"
            write_rgba_png(path, 768, 512, uniform=False)
            result = self.run_validator(path, "512x768")
            self.assertEqual(result.returncode, 0, result.stdout)

    def test_rejects_uniform_screenshot(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "screen.png"
            write_rgba_png(path, 1024, 1024, uniform=True)
            # Uniform PNG data compresses extremely well, so append inert bytes
            # to ensure this test reaches the pixel-uniformity validator rather
            # than failing first on the production 4096-byte integrity floor.
            with path.open("ab") as fh:
                fh.write(b"QA_PADDING" * 512)
            result = self.run_validator(path, "1024x1024")
            self.assertNotEqual(result.returncode, 0, result.stdout)

    def test_rejects_wrong_dimensions(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "screen.png"
            write_rgba_png(path, 512, 768, uniform=False)
            result = self.run_validator(path, "720x1600")
            self.assertNotEqual(result.returncode, 0, result.stdout)


if __name__ == "__main__":
    unittest.main()
