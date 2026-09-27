from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
VERIFIER = ROOT / ".github/scripts/verify-release-upgrade-state.py"


def varint(value: int) -> bytes:
    out = bytearray()
    while True:
        byte = value & 0x7F
        value >>= 7
        if value:
            out.append(byte | 0x80)
        else:
            out.append(byte)
            return bytes(out)


def field(number: int, wire: int, payload: bytes) -> bytes:
    tag = varint((number << 3) | wire)
    if wire == 0:
        return tag + payload
    if wire == 2:
        return tag + varint(len(payload)) + payload
    raise ValueError(wire)


def value_bool(value: bool) -> bytes:
    return field(1, 0, varint(1 if value else 0))


def value_string(value: str) -> bytes:
    return field(5, 2, value.encode("utf-8"))


def pref_entry(key: str, value_message: bytes) -> bytes:
    entry = field(1, 2, key.encode("utf-8")) + field(2, 2, value_message)
    return field(1, 2, entry)


def pref_map(entries: dict[str, bytes]) -> bytes:
    return b"".join(pref_entry(key, value) for key, value in entries.items())


class ReleaseUpgradeDatastoreVerifierTest(unittest.TestCase):
    def test_verifier_accepts_expected_seeded_state(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            profile = root / "profile.pb"
            behavior = root / "behavior.pb"
            goals = root / "goals.pb"

            profile.write_bytes(
                pref_map({
                    "onboarding_complete": value_bool(True),
                    "ads_removed": value_bool(True),
                })
            )
            behavior.write_bytes(
                pref_map({
                    "activity_enabled": value_bool(True),
                    "activity_state": value_string("AVAILABLE"),
                    "daily_behavior_v1": value_string("v1\n2026-09-27|4321|0|0|0|0|0|0|0"),
                })
            )
            goals.write_bytes(
                pref_map({
                    "goals_v1": value_string("v1\ndXBncmFkZS1wcm9iZS1nb2Fs|STEPS_AT_LEAST|4000||||"),
                })
            )

            result = subprocess.run(
                [
                    sys.executable,
                    str(VERIFIER),
                    "--profile", str(profile),
                    "--behavior", str(behavior),
                    "--goals", str(goals),
                ],
                text=True,
                capture_output=True,
                check=False,
            )

            self.assertEqual(0, result.returncode, result.stderr)
            self.assertIn("profile=onboarded", result.stdout)
            self.assertIn("behavior_steps=4321", result.stdout)
            self.assertIn("goal_target=4000", result.stdout)


if __name__ == "__main__":
    unittest.main()
