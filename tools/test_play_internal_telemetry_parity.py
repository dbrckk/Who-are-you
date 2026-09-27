from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/play-internal-publish.yml"


class PlayInternalTelemetryParityTest(unittest.TestCase):
    def test_internal_publish_can_match_candidate_telemetry_configuration(self):
        source = WORKFLOW.read_text(encoding="utf-8")

        self.assertIn("telemetry_enabled:", source)
        self.assertIn("WHO_ARE_YOU_TELEMETRY_ENDPOINT", source)
        self.assertIn("TELEMETRY_ENABLED: ${{ inputs.telemetry_enabled }}", source)
        self.assertIn(
            'telemetry_enabled=true requires WHO_ARE_YOU_TELEMETRY_ENDPOINT',
            source,
        )
        self.assertIn(
            'args+=("-PWHO_ARE_YOU_TELEMETRY_ENDPOINT=$TELEMETRY_ENDPOINT")',
            source,
        )


if __name__ == "__main__":
    unittest.main()
