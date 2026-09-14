#!/usr/bin/env python3
import argparse
import os
import struct
import sys

PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"

def parse_size(raw: str):
    try:
        width, height = raw.lower().split("x", 1)
        return int(width), int(height)
    except Exception as exc:
        raise ValueError(f"invalid display size: {raw!r}") from exc

def png_size(path: str):
    with open(path, "rb") as fh:
        signature = fh.read(8)
        if signature != PNG_SIGNATURE:
            raise ValueError("invalid PNG signature")
        length = struct.unpack(">I", fh.read(4))[0]
        chunk = fh.read(4)
        if chunk != b"IHDR" or length != 13:
            raise ValueError("missing PNG IHDR")
        width, height = struct.unpack(">II", fh.read(8))
        return width, height

def main(path: str, expected: str) -> int:
    expected_w, expected_h = parse_size(expected)
    size_bytes = os.path.getsize(path)
    if size_bytes < 4096:
        print(f"Screenshot too small: {size_bytes} bytes")
        return 1

    try:
        width, height = png_size(path)
    except Exception as exc:
        print(f"Invalid screenshot: {exc}")
        return 1

    valid_sizes = {(expected_w, expected_h), (expected_h, expected_w)}
    if (width, height) not in valid_sizes:
        print(
            f"Screenshot dimensions mismatch: got {width}x{height}px, "
            f"expected {expected_w}x{expected_h}px (or rotated)"
        )
        return 1

    print(
        f"Screenshot integrity OK: {width}x{height}px, "
        f"{size_bytes} bytes"
    )
    return 0

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("png")
    parser.add_argument("--size", required=True)
    args = parser.parse_args()
    raise SystemExit(main(args.png, args.size))
