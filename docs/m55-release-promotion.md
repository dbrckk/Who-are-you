# M55 — Guarded Google Play promotion pipeline

The repository separates **bundle publication** from **track promotion** so the same validated version can move through testing without rebuilding it.

## Track model

The Android Publisher API contract used by this repository is:

- `qa` — internal testing
- `beta` — open testing
- custom track identifier — closed testing track created in Play Console/API
- `production` — public production

Closed-testing tracks are intentionally not invented by CI. Their identifier must already exist in the Play account.

## Safety rules

`tools/play_publisher.py` and `tools/play_promoter.py` enforce:

- package is locked to `com.whoareyou.app`;
- production is rejected unless explicitly confirmed;
- staged rollout fractions are accepted only on production;
- staged rollout requires `0 < userFraction < 1`;
- `inProgress` production requires a rollout fraction;
- malformed track identifiers are rejected;
- Play edits are validated before commit;
- workflows do not commit changes by default.

## Internal upload

`.github/workflows/play-internal-publish.yml` builds the signed `playRelease` AAB, verifies it, uploads it, assigns it to the default internal track (`qa` through the publisher default), validates the Play edit, and commits only when `publish_to_play=true`.

## Promotion

`.github/workflows/play-promote.yml` promotes the already uploaded version without rebuilding or uploading another AAB.

Safe examples:

```bash
# Validate open-testing promotion only
python tools/play_publisher.py --validate-only --track beta --status draft

# Production payload validation requires explicit confirmation and staged fraction
python tools/play_publisher.py \
  --validate-only \
  --track production \
  --status inProgress \
  --user-fraction 0.05 \
  --confirm-production
```

The actual promotion workflow additionally requires `commit_edit=true` before any Play edit is committed.

## External dependencies

Before live use, the Play account must provide:

- a service account authorized for the application;
- the upload signing secrets for the initial bundle workflow;
- an existing closed-test track identifier if closed testing is desired;
- app signing certificate SHA-256 for verified App Links;
- finalized store/privacy/Data Safety declarations.

Production should stay behind a separately reviewed/protected GitHub environment before the first public rollout.
