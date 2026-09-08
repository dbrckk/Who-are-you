# Product analytics contract

Who Are You? uses a deliberately small product analytics surface. The goal is to understand activation, completion, retention, sharing and premium conversion without collecting user-authored text or high-granularity personality results.

## Funnel

1. `app_open`
2. `onboarding_view`
3. `onboarding_complete`
4. `screen_view`
5. `test_start`
6. `test_abandon` or `test_complete`
7. `result_view`
8. `result_share`, `challenge_create`, `challenge_open`, `challenge_complete`, `compatibility_share`
9. `premium_view`
10. `purchase_start`
11. `purchase_cancel` or `purchase_success`

Retention signals include `daily_question_view`, `daily_question_vote`, `streak_continue`, `achievement_unlock`, recommendation events and signature events.

## Privacy rules

- Do not send names, email addresses, free-form user text, device identifiers, advertising identifiers or purchase tokens.
- Quiz scores, compatibility values and signature confidence are sent only as coarse buckets.
- Event strings and parameters are length-limited before remote transmission.
- Unsupported parameter types are dropped.
- Remote non-fatal/fatal telemetry does not include exception messages or stack traces.
- Purchase tokens remain local to billing handling and are never analytics parameters.
- Deep-link telemetry records only a bounded route without query parameters.

## Score buckets

- `00_19`
- `20_39`
- `40_59`
- `60_79`
- `80_100`

## Interpretation

Primary activation KPI: onboarding completion followed by first `test_complete`.

Primary engagement KPI: repeat quiz completion plus retention events.

Primary social KPI: challenge creation to challenge completion conversion.

Primary monetization KPI: `purchase_success / premium_view`, with `purchase_start / premium_view` used to distinguish merchandising from billing-flow friction.
