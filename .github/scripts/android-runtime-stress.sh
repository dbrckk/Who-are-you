#!/usr/bin/env bash
set -euo pipefail

PACKAGE="com.whoareyou.app"
ACTIVITY="$PACKAGE/.MainActivity"
DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
CANDIDATE_APK="app/build/outputs/apk/candidate/app-candidate.apk"

gradle :app:assembleDebug :app:assembleCandidate --no-daemon --stacktrace

test -s "$DEBUG_APK"
test -s "$CANDIDATE_APK"

stress_apk() {
  local apk="$1"
  local label="$2"

  adb uninstall "$PACKAGE" >/dev/null 2>&1 || true
  adb install "$apk"
  adb shell am force-stop "$PACKAGE"
  adb logcat -c

  local start_output pid final_pid
  start_output="$(adb shell am start -W -n "$ACTIVITY")"
  printf '%s\n' "$start_output" | tee "device-startup-$label.txt"
  grep -F "Status: ok" "device-startup-$label.txt"

  pid="$(adb shell pidof "$PACKAGE" | tr -d '\r')"
  test -n "$pid"
  printf '%s PID: %s\n' "$label" "$pid"

  adb shell monkey \
    -p "$PACKAGE" \
    --throttle 75 \
    --ignore-crashes \
    --ignore-timeouts \
    --ignore-security-exceptions \
    150 \
    | tee "device-monkey-$label.txt"
  sleep 2

  adb logcat -d > "device-logcat-$label.txt"
  adb logcat -d AndroidRuntime:E '*:S' > "device-android-runtime-$label.txt"
  adb shell dumpsys activity exit-info "$PACKAGE" > "device-exit-info-$label.txt" || true

  final_pid="$(adb shell pidof "$PACKAGE" | tr -d '\r')"
  test -n "$final_pid"
  printf '%s final PID: %s\n' "$label" "$final_pid"

  if grep -F "Process: $PACKAGE" "device-android-runtime-$label.txt"; then
    echo "Fatal AndroidRuntime crash detected for $PACKAGE ($label)"
    exit 1
  fi

  if grep -E "REASON_(CRASH|ANR)" "device-exit-info-$label.txt"; then
    echo "Crash or ANR exit reason detected for $PACKAGE ($label)"
    exit 1
  fi
}

stress_apk "$DEBUG_APK" debug-stress
stress_apk "$CANDIDATE_APK" candidate-stress

echo "ATD debug + candidate runtime stress validation passed."
