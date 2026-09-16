#!/usr/bin/env bash
set -euo pipefail

PACKAGE="com.whoareyou.app"
ACTIVITY="$PACKAGE/.MainActivity"
DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
CANDIDATE_APK="app/build/outputs/apk/candidate/app-candidate.apk"
HOST_RESOURCE_LOG="device-host-resources.txt"
HOST_KERNEL_LOG="device-host-kernel.txt"
HOST_MONITOR_PID=""

capture_host_resource_snapshot() {
  {
    printf '\n=== %s ===\n' "$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
    echo "--- free -m ---"
    free -m || true
    echo "--- selected /proc/meminfo ---"
    grep -E '^(MemTotal|MemFree|MemAvailable|Buffers|Cached|SwapTotal|SwapFree):' /proc/meminfo || true
    echo "--- /proc/pressure/memory ---"
    if [[ -r /proc/pressure/memory ]]; then
      cat /proc/pressure/memory
    else
      echo "unavailable"
    fi
    echo "--- cgroup memory ---"
    for path in \
      /sys/fs/cgroup/memory.current \
      /sys/fs/cgroup/memory.max \
      /sys/fs/cgroup/memory.events; do
      echo "$path"
      if [[ -r "$path" ]]; then
        cat "$path"
      else
        echo "unavailable"
      fi
    done
    echo "--- top RSS processes ---"
    ps -eo pid,ppid,rss,vsz,%mem,%cpu,comm,args --sort=-rss | head -n 25 || true
    echo "--- emulator/qemu processes ---"
    pgrep -af 'emulator|qemu' || true
  } >> "$HOST_RESOURCE_LOG" 2>&1
}

capture_host_kernel_evidence() {
  {
    echo "=== dmesg tail ==="
    dmesg --ctime 2>&1 | tail -n 200 || true
    echo
    echo "=== kernel journal tail ==="
    journalctl -k --no-pager -n 200 2>&1 || true
  } > "$HOST_KERNEL_LOG"
}

start_host_resource_monitor() {
  : > "$HOST_RESOURCE_LOG"
  capture_host_resource_snapshot
  (
    while true; do
      sleep 5
      capture_host_resource_snapshot
    done
  ) &
  HOST_MONITOR_PID=$!
}

capture_exit_diagnostics() {
  local status=$?
  trap - EXIT
  set +e
  if [[ -n "${HOST_MONITOR_PID:-}" ]]; then
    kill "$HOST_MONITOR_PID" 2>/dev/null
    wait "$HOST_MONITOR_PID" 2>/dev/null
  fi
  capture_host_resource_snapshot
  capture_host_kernel_evidence
  exit "$status"
}

adb wait-for-device
test "$(adb shell getprop sys.boot_completed | tr -d '\r')" = "1"

gradle --stop || true
start_host_resource_monitor
trap capture_exit_diagnostics EXIT
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

  local size_line density_line display_size density_dpi
  size_line="$(adb shell wm size | tr -d '\r' | tail -n1)"
  density_line="$(adb shell wm density | tr -d '\r' | tail -n1)"
  display_size="${size_line##*: }"
  density_dpi="${density_line##*: }"

  python3 .github/scripts/validate-screenshot.py \
    "device-screen-$label.png" \
    --size "$display_size" \
    | tee "device-screen-report-$label.txt"

  python3 .github/scripts/validate-ui-hierarchy.py \
    "device-ui-$label.xml" \
    --package "$PACKAGE" \
    --size "$display_size" \
    --density "$density_dpi" \
    | tee "device-ui-report-$label.txt"
}

validate_evidence_matrix() {
  local labels=(
    "debug"
    "candidate"
    "upgrade-candidate"
    "upgrade-relaunch"
    "candidate-font-130"
    "candidate-compact"
    "candidate-large"
    "candidate-reduced-motion"
    "candidate-landscape"
    "candidate-compact-font-130"
  )

  : > device-validation-summary.txt
  for label in "${labels[@]}"; do
    for artifact in \
      "device-screen-$label.png" \
      "device-screen-report-$label.txt" \
      "device-ui-$label.xml" \
      "device-ui-report-$label.txt"; do
      test -s "$artifact"
      printf 'ok %s\n' "$artifact" >> device-validation-summary.txt
    done
  done
}

capture_reduced_motion_variant() {
  local label="$1"

  adb shell settings put global window_animation_scale 0
  adb shell settings put global transition_animation_scale 0
  adb shell settings put global animator_duration_scale 0
  adb shell am force-stop "$PACKAGE"
  START_OUTPUT="$(adb shell am start -W -n "$ACTIVITY")"
  printf '%s\n' "$START_OUTPUT" | tee "device-startup-$label.txt"
  grep -F "Status: ok" "device-startup-$label.txt"
  sleep 2
  capture_visual_evidence "$label"
  {
    echo "label=$label"
    echo "window_animation_scale=$(adb shell settings get global window_animation_scale | tr -d '\r')"
    echo "transition_animation_scale=$(adb shell settings get global transition_animation_scale | tr -d '\r')"
    echo "animator_duration_scale=$(adb shell settings get global animator_duration_scale | tr -d '\r')"
  } > "device-motion-$label.txt"
}

capture_landscape_variant() {
  local label="$1"

  adb shell settings put system accelerometer_rotation 0
  adb shell settings put system user_rotation 1
  adb shell am force-stop "$PACKAGE"
  START_OUTPUT="$(adb shell am start -W -n "$ACTIVITY")"
  printf '%s\n' "$START_OUTPUT" | tee "device-startup-$label.txt"
  grep -F "Status: ok" "device-startup-$label.txt"
  sleep 3
  capture_visual_evidence "$label"
  {
    echo "label=$label"
    echo "accelerometer_rotation=$(adb shell settings get system accelerometer_rotation | tr -d '\r')"
    echo "user_rotation=$(adb shell settings get system user_rotation | tr -d '\r')"
    adb shell dumpsys input | grep -m1 'SurfaceOrientation' || true
  } > "device-orientation-$label.txt"
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

capture_compact_accessibility_variant() {
  local label="$1"
  local size="$2"
  local density="$3"
  local font_scale="$4"

  adb shell wm size "$size"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font_scale"
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
capture_compact_accessibility_variant "candidate-compact-font-130" "720x1600" "320" "1.30"
capture_display_variant "candidate-large" "1600x2560" "320"
adb shell wm size reset
adb shell wm density reset

capture_reduced_motion_variant "candidate-reduced-motion"
adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1

capture_landscape_variant "candidate-landscape"
adb shell settings put system user_rotation 0
adb shell settings put system accelerometer_rotation 1

adb shell wm size reset
adb shell wm density reset
adb shell settings put system font_scale 1.0
adb shell am force-stop "$PACKAGE"

adb shell wm size > device-display-metrics.txt
adb shell wm density >> device-display-metrics.txt
adb shell settings get system font_scale >> device-display-metrics.txt

validate_evidence_matrix
python3 .github/scripts/summarize-visual-qa.py   --root .   --output device-visual-qa-summary.md

echo "Android debug + minified candidate + upgrade + accessibility + display-variant validation passed."
