#!/usr/bin/env bash
set -euo pipefail

PACKAGE="com.whoareyou.app"
ACTIVITY="$PACKAGE/.MainActivity"
APK="app/build/outputs/apk/debug/app-debug.apk"

adb wait-for-device
test "$(adb shell getprop sys.boot_completed | tr -d '\r')" = "1"

gradle --stop || true
gradle :app:connectedDebugAndroidTest --no-daemon --stacktrace
gradle :app:assembleDebug --no-daemon --stacktrace

test -s "$APK"
adb install -r "$APK"
adb shell am force-stop "$PACKAGE"
adb logcat -c

START_OUTPUT="$(adb shell am start -W -n "$ACTIVITY")"
printf '%s\n' "$START_OUTPUT" | tee device-startup.txt
grep -F "Status: ok" device-startup.txt
sleep 3

PID="$(adb shell pidof "$PACKAGE" | tr -d '\r')"
test -n "$PID"
printf 'Initial PID: %s\n' "$PID"

adb shell monkey -p "$PACKAGE" --throttle 75 --ignore-crashes --ignore-timeouts --ignore-security-exceptions 150 | tee device-monkey.txt
sleep 2

adb logcat -d > device-logcat.txt
adb logcat -d AndroidRuntime:E '*:S' > device-android-runtime.txt
adb shell dumpsys activity exit-info "$PACKAGE" > device-exit-info.txt || true

FINAL_PID="$(adb shell pidof "$PACKAGE" | tr -d '\r')"
test -n "$FINAL_PID"
printf 'Final PID: %s\n' "$FINAL_PID"

if grep -F "Process: $PACKAGE" device-android-runtime.txt; then
  echo "Fatal AndroidRuntime crash detected for $PACKAGE"
  exit 1
fi

if grep -E "REASON_(CRASH|ANR)" device-exit-info.txt; then
  echo "Crash or ANR exit reason detected for $PACKAGE"
  exit 1
fi

echo "Android device validation passed."
