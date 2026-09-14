#!/usr/bin/env python3
import argparse
import re
import sys
import xml.etree.ElementTree as ET

BOUNDS_RE = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")
SIZE_RE = re.compile(r"(\d+)x(\d+)")

def parse_bounds(raw: str):
    match = BOUNDS_RE.fullmatch(raw or "")
    if not match:
        return None
    return tuple(map(int, match.groups()))

def parse_size(raw: str):
    match = SIZE_RE.fullmatch(raw or "")
    if not match:
        raise ValueError(f"invalid display size: {raw!r}")
    return tuple(map(int, match.groups()))

def label_for(node):
    return (
        node.attrib.get("resource-id")
        or node.attrib.get("content-desc")
        or node.attrib.get("text")
        or node.attrib.get("class")
        or "<anonymous>"
    )

def main(path: str, package: str, display_size: str, density_dpi: int) -> int:
    screen_w, screen_h = parse_size(display_size)
    density = density_dpi / 160.0
    min_touch_px = 48.0 * density

    tree = ET.parse(path)
    root = tree.getroot()

    invalid = []
    zero_clickable = []
    undersized_clickable = []
    outside_viewport = []
    duplicate_ids = {}
    node_count = 0
    app_node_count = 0
    clickable_count = 0

    for node in root.iter("node"):
        node_count += 1
        node_package = node.attrib.get("package", "")
        if package and node_package and node_package != package:
            continue

        app_node_count += 1
        bounds = parse_bounds(node.attrib.get("bounds", ""))
        if bounds is None:
            invalid.append(label_for(node))
            continue

        x1, y1, x2, y2 = bounds
        if x2 < x1 or y2 < y1:
            invalid.append(label_for(node))
            continue

        if x1 < 0 or y1 < 0 or x2 > screen_w or y2 > screen_h:
            outside_viewport.append((label_for(node), bounds))

        clickable = node.attrib.get("clickable") == "true"
        enabled = node.attrib.get("enabled", "true") == "true"
        if clickable:
            clickable_count += 1
            width = x2 - x1
            height = y2 - y1
            if width <= 0 or height <= 0:
                zero_clickable.append(label_for(node))
            elif enabled and (width + 0.5 < min_touch_px or height + 0.5 < min_touch_px):
                undersized_clickable.append(
                    (label_for(node), width / density, height / density)
                )

        resource_id = node.attrib.get("resource-id", "")
        if resource_id:
            duplicate_ids[resource_id] = duplicate_ids.get(resource_id, 0) + 1

    repeated = sorted(k for k, count in duplicate_ids.items() if count > 1)

    print(
        f"UI hierarchy: {node_count} total nodes, {app_node_count} app nodes, "
        f"{clickable_count} clickable; viewport={screen_w}x{screen_h}px "
        f"density={density_dpi}dpi minTouch={min_touch_px:.1f}px"
    )

    if repeated:
        print(f"Repeated resource IDs: {len(repeated)}")
        for item in repeated[:20]:
            print(f"  - {item}")

    if invalid:
        print("Invalid bounds detected:")
        for item in invalid[:20]:
            print(f"  - {item}")

    if zero_clickable:
        print("Zero-size clickable nodes detected:")
        for item in zero_clickable[:20]:
            print(f"  - {item}")

    if undersized_clickable:
        print("Clickable nodes below 48dp touch target:")
        for item, width_dp, height_dp in undersized_clickable[:20]:
            print(f"  - {item}: {width_dp:.1f}x{height_dp:.1f}dp")

    if outside_viewport:
        print("Nodes outside viewport:")
        for item, bounds in outside_viewport[:20]:
            print(f"  - {item}: {bounds}")

    if invalid or zero_clickable or undersized_clickable or outside_viewport:
        return 1
    return 0

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("xml")
    parser.add_argument("--package", required=True)
    parser.add_argument("--size", required=True)
    parser.add_argument("--density", type=int, required=True)
    args = parser.parse_args()
    raise SystemExit(main(args.xml, args.package, args.size, args.density))
