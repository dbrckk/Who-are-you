#!/usr/bin/env python3
import re
import sys
import xml.etree.ElementTree as ET

BOUNDS_RE = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")

def parse_bounds(raw: str):
    match = BOUNDS_RE.fullmatch(raw or "")
    if not match:
        return None
    x1, y1, x2, y2 = map(int, match.groups())
    return x1, y1, x2, y2

def main(path: str) -> int:
    tree = ET.parse(path)
    root = tree.getroot()

    invalid = []
    zero_clickable = []
    duplicate_ids = {}
    node_count = 0
    clickable_count = 0

    for node in root.iter("node"):
        node_count += 1
        bounds = parse_bounds(node.attrib.get("bounds", ""))
        if bounds is None:
            invalid.append(node.attrib.get("resource-id") or node.attrib.get("text") or "<anonymous>")
            continue

        x1, y1, x2, y2 = bounds
        if x2 < x1 or y2 < y1:
            invalid.append(node.attrib.get("resource-id") or node.attrib.get("text") or "<anonymous>")

        clickable = node.attrib.get("clickable") == "true"
        if clickable:
            clickable_count += 1
            if x2 <= x1 or y2 <= y1:
                zero_clickable.append(node.attrib.get("resource-id") or node.attrib.get("text") or "<anonymous>")

        resource_id = node.attrib.get("resource-id", "")
        if resource_id:
            duplicate_ids[resource_id] = duplicate_ids.get(resource_id, 0) + 1

    repeated = sorted(k for k, count in duplicate_ids.items() if count > 1)

    print(f"UI hierarchy: {node_count} nodes, {clickable_count} clickable")
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

    if invalid or zero_clickable:
        return 1
    return 0

if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("usage: validate-ui-hierarchy.py <uiautomator.xml>")
    raise SystemExit(main(sys.argv[1]))
