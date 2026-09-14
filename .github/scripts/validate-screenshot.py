#!/usr/bin/env python3
import argparse
import os
import struct
import zlib

PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"

def parse_size(raw: str):
    try:
        width, height = raw.lower().split("x", 1)
        return int(width), int(height)
    except Exception as exc:
        raise ValueError(f"invalid display size: {raw!r}") from exc

def paeth(a: int, b: int, c: int) -> int:
    p = a + b - c
    pa = abs(p - a)
    pb = abs(p - b)
    pc = abs(p - c)
    if pa <= pb and pa <= pc:
        return a
    if pb <= pc:
        return b
    return c

def read_png(path: str):
    with open(path, "rb") as fh:
        if fh.read(8) != PNG_SIGNATURE:
            raise ValueError("invalid PNG signature")

        width = height = bit_depth = color_type = None
        idat = bytearray()

        while True:
            length_raw = fh.read(4)
            if not length_raw:
                break
            if len(length_raw) != 4:
                raise ValueError("truncated PNG chunk length")

            length = struct.unpack(">I", length_raw)[0]
            chunk_type = fh.read(4)
            data = fh.read(length)
            crc = fh.read(4)
            if len(chunk_type) != 4 or len(data) != length or len(crc) != 4:
                raise ValueError("truncated PNG chunk")

            if chunk_type == b"IHDR":
                if length != 13:
                    raise ValueError("invalid IHDR length")
                width, height, bit_depth, color_type, compression, filtering, interlace = struct.unpack(
                    ">IIBBBBB", data
                )
                if compression != 0 or filtering != 0 or interlace != 0:
                    raise ValueError("unsupported PNG encoding")
            elif chunk_type == b"IDAT":
                idat.extend(data)
            elif chunk_type == b"IEND":
                break

    if width is None or height is None:
        raise ValueError("missing PNG IHDR")
    if not idat:
        raise ValueError("missing PNG image data")
    if bit_depth != 8:
        raise ValueError(f"unsupported bit depth: {bit_depth}")

    channels = {
        0: 1,  # grayscale
        2: 3,  # RGB
        4: 2,  # grayscale + alpha
        6: 4,  # RGBA
    }.get(color_type)
    if channels is None:
        raise ValueError(f"unsupported color type: {color_type}")

    raw = zlib.decompress(bytes(idat))
    stride = width * channels
    expected = height * (stride + 1)
    if len(raw) != expected:
        raise ValueError(
            f"unexpected decoded PNG size: got {len(raw)}, expected {expected}"
        )

    rows = []
    previous = bytearray(stride)
    offset = 0

    for _ in range(height):
        filter_type = raw[offset]
        offset += 1
        scanline = raw[offset:offset + stride]
        offset += stride
        reconstructed = bytearray(stride)

        for i, value in enumerate(scanline):
            left = reconstructed[i - channels] if i >= channels else 0
            up = previous[i]
            upper_left = previous[i - channels] if i >= channels else 0

            if filter_type == 0:
                result = value
            elif filter_type == 1:
                result = (value + left) & 0xFF
            elif filter_type == 2:
                result = (value + up) & 0xFF
            elif filter_type == 3:
                result = (value + ((left + up) // 2)) & 0xFF
            elif filter_type == 4:
                result = (value + paeth(left, up, upper_left)) & 0xFF
            else:
                raise ValueError(f"unsupported PNG filter: {filter_type}")

            reconstructed[i] = result

        rows.append(reconstructed)
        previous = reconstructed

    return width, height, color_type, channels, rows

def screen_variation(width, height, color_type, channels, rows):
    total_pixels = width * height
    step = max(1, total_pixels // 6000)
    luminances = []
    buckets = set()

    index = 0
    for y, row in enumerate(rows):
        for x in range(width):
            if index % step:
                index += 1
                continue
            base = x * channels
            if color_type in (0, 4):
                r = g = b = row[base]
            else:
                r, g, b = row[base], row[base + 1], row[base + 2]

            luminance = (54 * r + 183 * g + 19 * b) // 256
            luminances.append(luminance)
            buckets.add((r // 16, g // 16, b // 16))
            index += 1

    if not luminances:
        raise ValueError("no pixels sampled")

    return min(luminances), max(luminances), len(buckets)

def main(path: str, expected: str) -> int:
    expected_w, expected_h = parse_size(expected)
    size_bytes = os.path.getsize(path)
    if size_bytes < 4096:
        print(f"Screenshot too small: {size_bytes} bytes")
        return 1

    try:
        width, height, color_type, channels, rows = read_png(path)
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

    try:
        luma_min, luma_max, bucket_count = screen_variation(
            width, height, color_type, channels, rows
        )
    except Exception as exc:
        print(f"Screenshot pixel analysis failed: {exc}")
        return 1

    luma_range = luma_max - luma_min
    if luma_range < 8 or bucket_count < 4:
        print(
            "Screenshot appears blank or nearly uniform: "
            f"lumaRange={luma_range}, colorBuckets={bucket_count}"
        )
        return 1

    print(
        f"Screenshot integrity OK: {width}x{height}px, {size_bytes} bytes, "
        f"lumaRange={luma_range}, colorBuckets={bucket_count}"
    )
    return 0

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("png")
    parser.add_argument("--size", required=True)
    args = parser.parse_args()
    raise SystemExit(main(args.png, args.size))
