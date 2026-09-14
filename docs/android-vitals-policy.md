# Android Vitals & Production Quality Policy

This document is the release policy for the public Google Play build.

## Google Play bad-behavior thresholds

As of September 2026, Google Play defines these overall bad-behavior thresholds for mobile apps:

- user-perceived crash rate: **1.09%** of daily users;
- user-perceived ANR rate: **0.47%** of daily active users;
- excessive partial wake locks: **5%** overall.

Google Play generally evaluates the most recent 28 days and can act sooner when a spike is detected.

Official references:
- https://support.google.com/googleplay/android-developer/answer/9844486
- https://developer.android.com/topic/performance/vitals

## Who Are You? internal production targets

The Play thresholds are ceilings, not targets. Use these stricter internal release targets:

- user-perceived crash rate: **< 0.75%** overall;
- user-perceived ANR rate: **< 0.30%** overall;
- no unresolved high-volume crash or ANR cluster introduced by the candidate;
- no unresolved pre-launch report blocker;
- no new device-model cluster approaching the Play per-device 8% threshold;
- no unexplained regression in memory, startup, rendering or battery indicators.

These internal targets are project policy, not Google Play requirements.

## Production promotion gate

Before promoting an Internal/Closed build to Production:

1. Review Android Vitals for the active test version and recent production versions.
2. Review user-perceived crash and ANR clusters.
3. Review the Play pre-launch report.
4. Review the Play candidate bundle budget report.
5. Review native-library inventory and 16 KB compatibility when native code exists.
6. Confirm that the R8 mapping file for the candidate is retained.
7. Use the `play-promote.yml` Production confirmations only after the above checks are complete.

## Bundle-size policy

The repository enforces an internal AAB budget:

- warning threshold: **30 MiB**;
- hard CI ceiling: **50 MiB**.

These are internal engineering budgets intended to catch accidental dependency/resource growth. They are not claimed as Google Play upload limits.

Every candidate produces a JSON bundle report with:
- AAB bytes and MiB;
- ZIP entry count;
- native-library count;
- DEX file count;
- resource entry count.

## Startup and rendering

The app reports `reportFullyDrawn()` only after the local profile and quiz catalog are available. Performance work should optimize both:
- time to initial display (TTID);
- time to fully drawn/useful state (TTFD).

For repeatable startup and frame measurements, use Jetpack Macrobenchmark rather than one-off stopwatch measurements.
