import unittest

import play_publisher


class PlayPromotionPayloadTest(unittest.TestCase):
    def test_internal_track_name_matches_current_play_api_contract(self):
        self.assertEqual(play_publisher.INTERNAL_TRACK, "qa")

    def test_closed_track_can_be_draft(self):
        payload = play_publisher.release_payload(track="closed-test", status="draft")
        self.assertEqual(payload["track"], "closed-test")

    def test_open_track_can_complete(self):
        payload = play_publisher.release_payload(track="beta", status="completed")
        self.assertEqual(payload["releases"][0]["status"], "completed")

    def test_production_needs_confirmation(self):
        with self.assertRaises(ValueError):
            play_publisher.release_payload(track="production", status="completed")

    def test_staged_production_payload(self):
        payload = play_publisher.release_payload(
            track="production",
            status="inProgress",
            user_fraction=0.05,
            allow_production=True,
        )
        release = payload["releases"][0]
        self.assertEqual(release["userFraction"], 0.05)
        self.assertEqual(release["status"], "inProgress")


if __name__ == "__main__":
    unittest.main()
