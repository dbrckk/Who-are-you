#!/usr/bin/env python3
import argparse
from pathlib import Path


def collect(root: Path):
    rows = []
    for ui_report in sorted(root.glob("device-ui-report-*.txt")):
        label = ui_report.stem.replace("device-ui-report-", "")
        screen_report = root / f"device-screen-report-{label}.txt"
        screenshot = root / f"device-screen-{label}.png"
        hierarchy = root / f"device-ui-{label}.xml"

        ui_text = ui_report.read_text(encoding="utf-8", errors="replace").strip()
        screen_text = screen_report.read_text(encoding="utf-8", errors="replace").strip() if screen_report.exists() else ""

        status = "PASS"
        if any(token in ui_text for token in (
            "Invalid bounds detected:",
            "Zero-size clickable nodes detected:",
            "Clickable nodes below 48dp touch target:",
            "Nodes outside viewport:",
        )):
            status = "FAIL"
        if "Screenshot integrity OK" not in screen_text:
            status = "FAIL"

        rows.append({
            "label": label,
            "status": status,
            "screenshot": screenshot.name if screenshot.exists() else "missing",
            "hierarchy": hierarchy.name if hierarchy.exists() else "missing",
            "screen": screen_text.splitlines()[-1] if screen_text else "missing screenshot report",
            "ui": ui_text.splitlines()[0] if ui_text else "missing UI report",
        })
    return rows


def main(root: Path, output: Path) -> int:
    rows = collect(root)
    if not rows:
        print("No visual QA reports found")
        return 1

    lines = [
        "# Visual QA Summary",
        "",
        "| Scenario | Status | Screenshot | UI hierarchy |",
        "| --- | --- | --- | --- |",
    ]
    for row in rows:
        lines.append(
            f"| {row['label']} | {row['status']} | {row['screen']} | {row['ui']} |"
        )

    failures = [row for row in rows if row["status"] != "PASS"]
    lines.extend([
        "",
        f"Scenarios: {len(rows)}",
        f"Failures: {len(failures)}",
        "",
    ])
    if failures:
        lines.append("## Failed scenarios")
        lines.append("")
        for row in failures:
            lines.append(f"- {row['label']}")
    else:
        lines.append("All captured visual QA scenarios passed.")

    output.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(output.read_text(encoding="utf-8"))
    return 1 if failures else 0


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--output", default="device-visual-qa-summary.md")
    args = parser.parse_args()
    raise SystemExit(main(Path(args.root), Path(args.output)))
