from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
CHECKLIST = ROOT / "docs/play-release-checklist.md"


class ReleaseArtifactScopeChecklistTest(unittest.TestCase):
    def test_candidate_and_play_signed_acceptance_scopes_are_explicit(self):
        source = CHECKLIST.read_text(encoding="utf-8").lower()

        required = (
            "candidate apk has external services disabled",
            "do not use the candidate apk to validate production admob or play billing",
            "play-signed internal testing build",
            "validate consent, ads and billing only on the play-signed internal testing build",
        )
        for phrase in required:
            self.assertIn(phrase, source)


if __name__ == "__main__":
    unittest.main()
