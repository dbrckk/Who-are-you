#!/usr/bin/env bash
set -euo pipefail

PACKAGE="com.whoareyou.app"
ACTIVITY="$PACKAGE/.MainActivity"
DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
CANDIDATE_APK="app/build/outputs/apk/candidate/app-candidate.apk"

adb wait-for-device
test "$(adb shell getprop sys.boot_completed | tr -d '\r')" = "1"

gradle --stop || true
gradle :app:connectedDebugAndroidTest --no-daemon --stacktrace
gradle :app:assembleDebug :app:assembleCandidate --no-daemon --stacktrace

test -s "$DEBUG_APK"
test -s "$CANDIDATE_APK"

smoke_apk() {
  local apk="$1"
  local label="$2"

  adb uninstall "$PACKAGE" >/dev/null 2>&1 || true
  adb install "$apk"
  adb shell am force-stop "$PACKAGE"
  adb logcat -c

  START_OUTPUT="$(adb shell am start -W -n "$ACTIVITY")"
  printf '%s\n' "$START_OUTPUT" | tee "device-startup-$label.txt"
  grep -F "Status: ok" "device-startup-$label.txt"
  sleep 3

  PID="$(adb shell pidof "$PACKAGE" | tr -d '\r')"
  test -n "$PID"
  printf '%s PID: %s\n' "$label" "$PID"

  adb shell monkey -p "$PACKAGE" --throttle 75 --ignore-crashes --ignore-timeouts --ignore-security-exceptions 150 | tee "device-monkey-$label.txt"
  sleep 2

  adb logcat -d > "device-logcat-$label.txt"
  adb logcat -d AndroidRuntime:E '*:S' > "device-android-runtime-$label.txt"
  adb shell dumpsys activity exit-info "$PACKAGE" > "device-exit-info-$label.txt" || true

  FINAL_PID="$(adb shell pidof "$PACKAGE" | tr -d '\r')"
  test -n "$FINAL_PID"
  printf '%s final PID: %s\n' "$label" "$FINAL_PID"

  if grep -F "Process: $PACKAGE" "device-android-runtime-$label.txt"; then
    echo "Fatal AndroidRuntime crash detected for $PACKAGE ($label)"
    exit 1
  fi

  if grep -E "REASON_(CRASH|ANR)" "device-exit-info-$label.txt"; then
    echo "Crash or ANR exit reason detected for $PACKAGE ($label)"
    exit 1
  fi
}

smoke_apk "$DEBUG_APK" debug
smoke_apk "$CANDIDATE_APK" candidate

echo "Android debug + minified candidate device validation passed."
