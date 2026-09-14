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

capture_visual_evidence() {
  local label="$1"

  adb exec-out screencap -p > "device-screen-$label.png"
  test -s "device-screen-$label.png"
  adb shell uiautomator dump "/sdcard/device-ui-$label.xml" >/dev/null
  adb pull "/sdcard/device-ui-$label.xml" "device-ui-$label.xml" >/dev/null
  test -s "device-ui-$label.xml"
  python3 .github/scripts/validate-ui-hierarchy.py "device-ui-$label.xml" | tee "device-ui-report-$label.txt"
}

capture_display_variant() {
  local label="$1"
  local size="$2"
  local density="$3"

  adb shell wm size "$size"
  adb shell wm density "$density"
  adb shell settings put system font_scale 1.0
  adb shell am force-stop "$PACKAGE"
  START_OUTPUT="$(adb shell am start -W -n "$ACTIVITY")"
  printf '%s\n' "$START_OUTPUT" | tee "device-startup-$label.txt"
  grep -F "Status: ok" "device-startup-$label.txt"
  sleep 3
  capture_visual_evidence "$label"
  {
    echo "label=$label"
    adb shell wm size
    adb shell wm density
    printf 'font_scale='
    adb shell settings get system font_scale
  } > "device-display-$label.txt"
}

capture_accessibility_variant() {
  local label="$1"
  local font_scale="$2"

  adb shell settings put system font_scale "$font_scale"
  adb shell am force-stop "$PACKAGE"
  START_OUTPUT="$(adb shell am start -W -n "$ACTIVITY")"
  printf '%s\n' "$START_OUTPUT" | tee "device-startup-$label.txt"
  grep -F "Status: ok" "device-startup-$label.txt"
  sleep 3
  capture_visual_evidence "$label"
}

validate_running_app() {
  local label="$1"

  START_OUTPUT="$(adb shell am start -W -n "$ACTIVITY")"
  printf '%s\n' "$START_OUTPUT" | tee "device-startup-$label.txt"
  grep -F "Status: ok" "device-startup-$label.txt"
  sleep 3

  capture_visual_evidence "$label"

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

smoke_apk() {
  local apk="$1"
  local label="$2"

  adb uninstall "$PACKAGE" >/dev/null 2>&1 || true
  adb install "$apk"
  adb shell am force-stop "$PACKAGE"
  adb logcat -c
  validate_running_app "$label"
}

smoke_apk "$DEBUG_APK" debug
smoke_apk "$CANDIDATE_APK" candidate

# Validate the real upgrade path: initialize data with the debug build, then
# replace it in-place with the minified candidate without clearing app data.
adb uninstall "$PACKAGE" >/dev/null 2>&1 || true
adb install "$DEBUG_APK"
adb shell am force-stop "$PACKAGE"
adb logcat -c
UPGRADE_DEBUG_START="$(adb shell am start -W -n "$ACTIVITY")"
printf '%s\n' "$UPGRADE_DEBUG_START" | tee device-startup-upgrade-debug.txt
grep -F "Status: ok" device-startup-upgrade-debug.txt
sleep 2
adb shell am force-stop "$PACKAGE"

adb install -r "$CANDIDATE_APK"
adb shell am force-stop "$PACKAGE"
adb logcat -c
validate_running_app upgrade-candidate

# One more cold relaunch after the upgrade catches startup failures that only
# appear after process death with migrated/persisted state.
adb shell am force-stop "$PACKAGE"
adb logcat -c
RELAUNCH_OUTPUT="$(adb shell am start -W -n "$ACTIVITY")"
printf '%s\n' "$RELAUNCH_OUTPUT" | tee device-startup-upgrade-relaunch.txt
grep -F "Status: ok" device-startup-upgrade-relaunch.txt
sleep 3
capture_visual_evidence "upgrade-relaunch"
RELAUNCH_PID="$(adb shell pidof "$PACKAGE" | tr -d '\r')"
test -n "$RELAUNCH_PID"
adb logcat -d AndroidRuntime:E '*:S' > device-android-runtime-upgrade-relaunch.txt
adb shell dumpsys activity exit-info "$PACKAGE" > device-exit-info-upgrade-relaunch.txt || true
if grep -F "Process: $PACKAGE" device-android-runtime-upgrade-relaunch.txt; then
  echo "Fatal AndroidRuntime crash detected after upgraded cold relaunch"
  exit 1
fi
if grep -E "REASON_(CRASH|ANR)" device-exit-info-upgrade-relaunch.txt; then
  echo "Crash or ANR exit reason detected after upgraded cold relaunch"
  exit 1
fi

capture_accessibility_variant "candidate-font-130" "1.30"
adb shell settings put system font_scale 1.0

capture_display_variant "candidate-compact" "720x1600" "320"
capture_display_variant "candidate-large" "1600x2560" "320"
adb shell wm size reset
adb shell wm density reset
adb shell settings put system font_scale 1.0
adb shell am force-stop "$PACKAGE"

adb shell wm size > device-display-metrics.txt
adb shell wm density >> device-display-metrics.txt
adb shell settings get system font_scale >> device-display-metrics.txt

echo "Android debug + minified candidate + in-place upgrade + accessibility validation passed."
