#!/usr/bin/env python3
from __future__ import annotations

import argparse
import base64
from pathlib import Path


def read_varint(data: bytes, offset: int) -> tuple[int, int]:
    value = 0
    shift = 0
    while True:
        if offset >= len(data):
            raise ValueError("truncated varint")
        byte = data[offset]
        offset += 1
        value |= (byte & 0x7F) << shift
        if not byte & 0x80:
            return value, offset
        shift += 7
        if shift >= 70:
            raise ValueError("invalid varint")


def fields(data: bytes):
    offset = 0
    while offset < len(data):
        tag, offset = read_varint(data, offset)
        number = tag >> 3
        wire = tag & 7
        if wire == 0:
            value, offset = read_varint(data, offset)
            yield number, wire, value
        elif wire == 1:
            end = offset + 8
            if end > len(data):
                raise ValueError("truncated fixed64")
            yield number, wire, data[offset:end]
            offset = end
        elif wire == 2:
            size, offset = read_varint(data, offset)
            end = offset + size
            if end > len(data):
                raise ValueError("truncated length-delimited field")
            yield number, wire, data[offset:end]
            offset = end
        elif wire == 5:
            end = offset + 4
            if end > len(data):
                raise ValueError("truncated fixed32")
            yield number, wire, data[offset:end]
            offset = end
        else:
            raise ValueError(f"unsupported protobuf wire type {wire}")


def parse_preferences(path: Path) -> dict[str, object]:
    result: dict[str, object] = {}
    for number, wire, entry_bytes in fields(path.read_bytes()):
        if number != 1 or wire != 2:
            continue
        key = None
        value_message = None
        for entry_number, entry_wire, entry_value in fields(entry_bytes):
            if entry_number == 1 and entry_wire == 2:
                key = entry_value.decode("utf-8")
            elif entry_number == 2 and entry_wire == 2:
                value_message = entry_value
        if key is None or value_message is None:
            continue
        decoded: object | None = None
        for value_number, value_wire, value in fields(value_message):
            if value_number == 1 and value_wire == 0:
                decoded = bool(value)
            elif value_number == 4 and value_wire == 0:
                decoded = int(value)
            elif value_number == 5 and value_wire == 2:
                decoded = value.decode("utf-8")
        if decoded is not None:
            result[key] = decoded
    return result


def decode_text(value: str) -> str:
    padding = "=" * (-len(value) % 4)
    return base64.urlsafe_b64decode(value + padding).decode("utf-8")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--profile", type=Path, required=True)
    parser.add_argument("--behavior", type=Path, required=True)
    parser.add_argument("--goals", type=Path, required=True)
    args = parser.parse_args()

    profile = parse_preferences(args.profile)
    behavior = parse_preferences(args.behavior)
    goals = parse_preferences(args.goals)

    assert profile.get("onboarding_complete") is True, profile
    assert profile.get("ads_removed") is True, profile
    assert behavior.get("activity_enabled") is True, behavior
    assert behavior.get("activity_state") == "AVAILABLE", behavior

    history = behavior.get("daily_behavior_v1")
    assert isinstance(history, str), behavior
    day_lines = history.splitlines()
    assert len(day_lines) >= 2, history
    day_fields = day_lines[1].split("|", 8)
    assert len(day_fields) == 9, day_fields
    assert int(day_fields[1]) == 4321, day_fields

    encoded_goals = goals.get("goals_v1")
    assert isinstance(encoded_goals, str), goals
    goal_lines = encoded_goals.splitlines()
    assert len(goal_lines) >= 2, encoded_goals
    goal_fields = goal_lines[1].split("|", 6)
    assert len(goal_fields) == 7, goal_fields
    assert decode_text(goal_fields[0]) == "upgrade-probe-goal", goal_fields
    assert goal_fields[1] == "STEPS_AT_LEAST", goal_fields
    assert int(goal_fields[2]) == 4000, goal_fields

    print("profile=onboarded")
    print("ads_removed=true")
    print("activity_state=AVAILABLE")
    print("behavior_steps=4321")
    print("goal_id=upgrade-probe-goal")
    print("goal_target=4000")


if __name__ == "__main__":
    main()
