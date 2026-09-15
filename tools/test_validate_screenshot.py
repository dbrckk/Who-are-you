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


def png_chunk(kind: bytes, data: bytes) -> bytes:
    payload = kind + data
    return (
        struct.pack(">I", len(data))
        + payload
        + struct.pack(">I", zlib.crc32(payload) & 0xFFFFFFFF)
    )


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

    png = bytearray(b"\x89PNG\r\n\x1a\n")
    png.extend(png_chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)))
    png.extend(png_chunk(b"IDAT", zlib.compress(bytes(rows), 6)))
    png.extend(png_chunk(b"IEND", b""))
    path.write_bytes(png)


def write_highly_compressed_non_uniform_png(path: Path, width: int, height: int) -> None:
    palette = (
        (16, 16, 16, 255),
        (64, 64, 64, 255),
        (160, 80, 32, 255),
        (240, 240, 240, 255),
    )
    rows = bytearray()
    band_height = max(1, height // len(palette))
    for y in range(height):
        rows.append(0)  # filter: None
        color = palette[min(y // band_height, len(palette) - 1)]
        for _ in range(width):
            rows.extend(color)

    png = bytearray(b"\x89PNG\r\n\x1a\n")
    png.extend(png_chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)))
    png.extend(png_chunk(b"IDAT", zlib.compress(bytes(rows), 9)))
    png.extend(png_chunk(b"IEND", b""))
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

    def test_accepts_highly_compressed_non_uniform_screenshot(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "screen.png"
            write_highly_compressed_non_uniform_png(path, 320, 640)
            self.assertLess(path.stat().st_size, 4096)
            result = self.run_validator(path, "320x640")
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
