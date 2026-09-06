# M24 — Recommendation completion attribution

Goal: complete the recommendation analytics funnel without leaking profile identity or scores.

Events:
- `recommendation_view`
- `recommendation_start`
- `recommendation_complete`

All recommendation funnel events carry only:
- `quiz_id`
- `mode` (`generic` or `signature_guided`)

Completion is attributed only when the quiz attempt was launched from the recommended card and the completed quiz matches that attributed attempt. Launching the same quiz from the regular catalog must not count as a recommendation completion.
