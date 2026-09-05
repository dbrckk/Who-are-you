# M11 — Quality, monetization robustness and progression polish

Focus this milestone on product quality rather than feature count.

## Targets
- Prevent a failed interstitial show from consuming the 7-minute frequency cap.
- Support modern Google Play Billing one-time offers by carrying the selected offer token into the purchase flow.
- Make the “complete profile” achievement depend on the current quiz catalog size instead of a hard-coded 15.
- Keep all changes backward compatible and CI-green.

## Follow-ups considered after this pass
- Persist social match history for Strong Match / Opposites achievements.
- Persist announced achievement IDs so unlock telemetry is exactly-once.
- Add a lightweight settings/privacy surface before public release.
