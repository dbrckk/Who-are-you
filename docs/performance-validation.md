# Performance validation

## Why this exists

Google recommends Macrobenchmark for repeatable startup and frame measurements and Baseline Profiles for improving fresh-install performance.

The repository now contains:
- `:baseline-profile` — Baseline Profile + Macrobenchmark producer;
- `StartupBenchmark` — cold and warm startup timing;
- `BaselineProfileGenerator` — startup/profile generation;
- `reportFullyDrawn()` — TTFD signal after local profile and catalog load;
- Play bundle budget reporting.

Official references:
- https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview
- https://developer.android.com/topic/performance/baselineprofiles/measure-baselineprofile
- https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile

## Generate the Baseline Profile

Use an Android 13+ connected test device. A physical device is preferred.

```bash
gradle :app:generateBaselineProfile
```

The generated profile is copied into the app Baseline Profile source set by the Gradle plugin.

After generation:
1. review the generated rules;
2. build the benchmark/release-like variant;
3. rerun startup benchmarks;
4. commit the generated profile only when the measured result is neutral or better.

## Measure startup

Run the Macrobenchmark on a physical device:

```bash
gradle :baseline-profile:connectedBenchmarkAndroidTest
```

or run the individual `StartupBenchmark` from Android Studio.

Measure at least:
- cold startup with Baseline Profile;
- warm startup with Baseline Profile;
- TTID from `StartupTimingMetric`;
- TTFD from the app's `reportFullyDrawn()` signal.

Do not use emulator timing as a production performance target. Emulator CPU/storage resources are shared with the host and are not representative.

## Regression policy

Before a production promotion:
- rerun startup benchmark after major dependency, startup, navigation, DataStore or SDK-init changes;
- investigate any repeatable median cold-start regression > 10%;
- investigate any repeatable median warm-start regression > 10%;
- do not accept a regression simply because it remains below a generic absolute number;
- compare on the same physical device, OS version, thermal state and benchmark configuration.

The 10% threshold is an internal regression trigger, not a Google Play requirement.

## Bundle budget

Every Play candidate and internal publish produces a bundle report.

Internal policy:
- <= 30 MiB: OK;
- > 30 MiB: warning;
- > 50 MiB: build failure.

Review dependency growth whenever the report changes materially.

## Android Vitals

See `docs/android-vitals-policy.md`.

Production promotion requires explicit confirmation that:
- Android Vitals were reviewed;
- the Play pre-launch report was reviewed;
- no release-blocking regression remains.
