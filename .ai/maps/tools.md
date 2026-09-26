This file is a merged representation of a subset of the codebase, containing specifically included files and files not matching ignore patterns, combined into a single document by Repomix.
The content has been processed where content has been compressed (code blocks are separated by ⋮---- delimiter).

# File Summary

## Purpose
This file contains a packed representation of a subset of the repository's contents that is considered the most important context.
It is designed to be easily consumable by AI systems for analysis, code review,
or other automated processes.

## File Format
The content is organized as follows:
1. This summary section
2. Repository information
3. Directory structure
4. Repository files (if enabled)
5. Multiple file entries, each consisting of:
  a. A header with the file path (## File: path/to/file)
  b. The full contents of the file in a code block

## Usage Guidelines
- This file should be treated as read-only. Any changes should be made to the
  original repository files, not this packed version.
- When processing this file, use the file path to distinguish
  between different files in the repository.
- Be aware that this file may contain sensitive information. Handle it with
  the same level of security as you would the original repository.

## Notes
- Some files may have been excluded based on .gitignore rules and Repomix's configuration
- Binary files are not included in this packed representation. Please refer to the Repository Structure section for a complete list of file paths, including binary files
- Only files matching these patterns are included: **/*.{py,js,mjs,cjs,ts,tsx,jsx,java,kt,kts,gd,groovy,gradle,toml,json,yaml,yml,sql,sh}
- Files matching these patterns are excluded: .ai/**, **/node_modules/**, **/.gradle/**, **/build/**, **/dist/**, **/.venv/**, **/__pycache__/**, **/.pytest_cache/**, **/.git/**, **/coverage/**, **/*.lock, **/*.min.js, **/*.map, assets/**, art/**, art_sources/**, marketing/**, colab/**, kaggle/**, discovery-cache.json, health-snapshot.json, history.json
- Files matching patterns in .gitignore are excluded
- Files matching default ignore patterns are excluded
- Content has been compressed - code blocks are separated by ⋮---- delimiter
- Files are sorted by Git change count (files with more changes are at the bottom)

# Directory Structure
```
check_play_bundle_budget.py
play_preflight.py
play_promoter.py
play_publisher.py
prepare_play_submission.py
test_accessibility_launch_contract.py
test_accessibility_system_contract.py
test_actionable_knowledge_map_contract.py
test_android_ci_sdk_setup_contract.py
test_baseline_profile_manifest_contract.py
test_battery_thermal_contract.py
test_behavior_copy_contract.py
test_behavior_goal_copy_contract.py
test_behavior_goal_privacy_contract.py
test_behavior_goal_storage_contract.py
test_behavior_goal_v1_scope_contract.py
test_behavior_integration_contract.py
test_behavior_privacy_contract.py
test_billing_launch_readiness.py
test_challenge_landing_contract.py
test_gradle_release_reproducibility.py
test_healthy_discover_profile_contract.py
test_large_screen_keyboard_accessibility.py
test_local_profile_privacy_contract.py
test_localization_parity.py
test_longitudinal_trend_integration.py
test_m33_readiness.py
test_m46_accessibility.py
test_m47_ui_test_wiring.py
test_m48_visual_system.py
test_m49_architecture_cleanup.py
test_m50_analytics.py
test_m51_release_readiness.py
test_m52_play_policy.py
test_m53_publication_contract.py
test_m54_play_preflight.py
test_m55_release_automation.py
test_m56_release_candidate.py
test_m59_host_diagnostics_contract.py
test_m59_no_kvm_contract.py
test_m59_runtime_stress_split_contract.py
test_m59_split_validation_contract.py
test_m59_visual_emulator_contract.py
test_m769_landscape_viewport.py
test_m771_localization.py
test_m771_result_intelligence_catalog.py
test_main_activity_architecture.py
test_main_activity_recreation_sync_contract.py
test_main_flow_recommendation_parity_contract.py
test_manifest_security_contract.py
test_offline_resilience_contract.py
test_performance_release_gate_contract.py
test_persisted_result_score_contract.py
test_play_2026_readiness.py
test_play_bundle_budget.py
test_play_candidate_workflow.py
test_play_promoter.py
test_play_publish_pipeline_contract.py
test_play_publish_workflow.py
test_play_publisher.py
test_play_release_safety_contract.py
test_prepare_play_submission.py
test_profile_coverage_integration.py
test_profile_knowledge_map_contract.py
test_profile_narrative_integration.py
test_profile_reset_contract.py
test_profile_result_allocation_contract.py
test_profile_startup_resilience.py
test_progression_journal_integration.py
test_public_launch_experience_contract.py
test_public_result_and_store_positioning.py
test_quiz_commit_interaction_contract.py
test_quiz_commit_interaction_lock.py
test_quiz_process_recreation_contract.py
test_quiz_replay_window_contract.py
test_quiz_result_persistence_feedback.py
test_reduced_motion_large_font_contract.py
test_release_ci_contract.py
test_release_critical_profile_contract.py
test_release_integration_contract.py
test_release_workflows_contract.py
test_rendering_performance_contract.py
test_restored_quiz_recovery.py
test_result_visual_hierarchy_contract.py
test_runtime_efficiency_contract.py
test_runtime_memory_lifecycle.py
test_runtime_performance_contract.py
test_share_rendering_performance.py
test_share_storage_safety.py
test_shell_accessibility_contract.py
test_startup_loading_state.py
test_string_resource_parity.py
test_summarize_visual_qa.py
test_trait_evolution_integration.py
test_trait_graph_contract.py
test_trait_taxonomy_v2.py
test_trait_timeline_integration.py
test_validate_screenshot.py
test_validate_ui_hierarchy.py
test_workmanager_r8_contract.py
validate_public_release_inputs.py
```

# Files

## File: check_play_bundle_budget.py
```python
#!/usr/bin/env python3
⋮----
MIB = 1024 * 1024
DEFAULT_WARN_MIB = 30
DEFAULT_FAIL_MIB = 50
⋮----
def inspect_bundle(path: Path) -> dict
⋮----
size_bytes = path.stat().st_size
⋮----
entries = archive.infolist()
native = [e for e in entries if "/lib/" in f"/{e.filename}" and e.filename.endswith(".so")]
dex = [e for e in entries if e.filename.endswith(".dex")]
resources = [e for e in entries if "/res/" in f"/{e.filename}"]
⋮----
def main() -> int
⋮----
parser = argparse.ArgumentParser(description="Inspect and enforce the internal Play AAB size budget.")
⋮----
args = parser.parse_args()
⋮----
report = inspect_bundle(args.bundle)
⋮----
rendered = json.dumps(report, indent=2, sort_keys=True)
```

## File: play_preflight.py
```python
#!/usr/bin/env python3
⋮----
ROOT = Path(__file__).resolve().parents[1]
⋮----
def check_repository(root: Path) -> list[dict]
⋮----
checks = []
⋮----
def add(name: str, ok: bool, detail: str)
⋮----
gradle = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
ad_manager = (root / "app/src/main/java/com/whoareyou/app/AdManager.kt").read_text(encoding="utf-8")
billing = (root / "app/src/main/java/com/whoareyou/app/BillingManager.kt").read_text(encoding="utf-8")
privacy = (root / "docs/privacy-policy.md").read_text(encoding="utf-8")
data_safety = (root / "docs/play-data-safety.md").read_text(encoding="utf-8")
app_links = (root / "docs/APP_LINKS.md").read_text(encoding="utf-8")
⋮----
def run(root: Path, public_inputs: Path | None, strict_public: bool) -> dict
⋮----
checks = check_repository(root)
repo_ready = all(item["ok"] for item in checks)
⋮----
external = {
⋮----
payload = load_public_inputs(public_inputs)
⋮----
public_ready = repo_ready and external["ready"] if strict_public else repo_ready
⋮----
def main() -> int
⋮----
parser = argparse.ArgumentParser(description="Autonomous Google Play release readiness preflight")
⋮----
args = parser.parse_args()
⋮----
report = run(args.repo_root, args.public_inputs, args.strict_public)
encoded = json.dumps(report, indent=2, sort_keys=True)
```

## File: play_promoter.py
```python
#!/usr/bin/env python3
"""Promote an already-uploaded Play version to another track without re-uploading it."""
⋮----
config = ReleaseConfig(track=track, version_code=version_code)
⋮----
payload = release_payload(
⋮----
session = _authorized_session(credentials_path)
⋮----
response = session.post(f"{API_ROOT}/{PACKAGE_NAME}/edits", json={}, timeout=60)
⋮----
edit_id = response.json()["id"]
⋮----
update = session.put(
⋮----
validation = session.post(endpoint(PACKAGE_NAME, edit_id, "validate"), json={}, timeout=60)
⋮----
result: dict[str, Any] = {
⋮----
committed = session.post(endpoint(PACKAGE_NAME, edit_id, "commit"), json={}, timeout=60)
⋮----
def main(argv: list[str] | None = None) -> int
⋮----
parser = argparse.ArgumentParser(description="Guarded Google Play track promotion")
⋮----
args = parser.parse_args(argv)
⋮----
result = promote_version(
```

## File: play_publisher.py
```python
#!/usr/bin/env python3
"""Google Play publishing helper with explicit track and rollout safeguards.

Request/payload construction stays deterministic and testable without Google
credentials. Network calls are made only by publish_bundle(). Production is
opt-in and requires an explicit confirmation flag.
"""
⋮----
PACKAGE_NAME = "com.whoareyou.app"
INTERNAL_TRACK = "qa"
OPEN_TEST_TRACK = "beta"
PRODUCTION_TRACK = "production"
VERSION_CODE = 1
VERSION_NAME = "0.1.0"
API_ROOT = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications"
UPLOAD_ROOT = "https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"
TRACK_RE = re.compile(r"^[a-zA-Z0-9._-]{1,80}$")
RELEASE_STATUSES = {"draft", "inProgress", "halted", "completed"}
⋮----
@dataclass(frozen=True)
class ReleaseConfig
⋮----
package_name: str = PACKAGE_NAME
track: str = INTERNAL_TRACK
version_code: int = VERSION_CODE
version_name: str = VERSION_NAME
⋮----
def validate(self, *, allow_production: bool = False) -> None
⋮----
def validate_track(track: str, *, allow_production: bool = False) -> str
⋮----
release: dict[str, Any] = {
⋮----
def endpoint(package_name: str, edit_id: str, resource: str) -> str
⋮----
def load_service_account(path: pathlib.Path) -> dict[str, Any]
⋮----
data = json.loads(path.read_text(encoding="utf-8"))
required = {"type", "client_email", "private_key", "token_uri"}
missing = sorted(required - data.keys())
⋮----
def _authorized_session(credentials_path: pathlib.Path)
⋮----
except ImportError as exc:  # pragma: no cover
⋮----
credentials = service_account.Credentials.from_service_account_file(
⋮----
config = ReleaseConfig(track=track)
⋮----
payload = release_payload(
⋮----
session = _authorized_session(credentials_path)
response = session.post(f"{API_ROOT}/{config.package_name}/edits", json={}, timeout=60)
⋮----
edit_id = response.json()["id"]
⋮----
upload_url = f"{UPLOAD_ROOT}/{config.package_name}/edits/{edit_id}/bundles"
⋮----
upload = session.post(
⋮----
uploaded_version = int(upload.json()["versionCode"])
⋮----
track_url = endpoint(config.package_name, edit_id, f"tracks/{track}")
track_update = session.put(track_url, json=payload, timeout=60)
⋮----
validate = session.post(endpoint(config.package_name, edit_id, "validate"), json={}, timeout=60)
⋮----
result: dict[str, Any] = {
⋮----
commit_response = session.post(endpoint(config.package_name, edit_id, "commit"), json={}, timeout=60)
⋮----
def main(argv: list[str] | None = None) -> int
⋮----
parser = argparse.ArgumentParser()
⋮----
args = parser.parse_args(argv)
⋮----
config = ReleaseConfig(track=args.track)
⋮----
result = publish_bundle(
```

## File: prepare_play_submission.py
```python
#!/usr/bin/env python3
⋮----
PACKAGE_NAME = "com.whoareyou.app"
PLACEHOLDER_MARKERS = ("REPLACE", "example.com", "REPLACE_BEFORE", "REPLACE_AFTER")
EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")
ADMOB_APP_RE = re.compile(r"^ca-app-pub-\d{16}~\d{10}$")
ADMOB_UNIT_RE = re.compile(r"^ca-app-pub-\d{16}/\d{10}$")
SHA256_RE = re.compile(r"^(?:[0-9A-Fa-f]{2}:){31}[0-9A-Fa-f]{2}$")
⋮----
MARKDOWN_CONTACT_PLACEHOLDER = (
HTML_CONTACT_PLACEHOLDER = (
HTML_PRERELEASE_NOTICE = (
⋮----
class InputError(ValueError)
⋮----
def _required_string(payload, key)
⋮----
value = payload.get(key)
⋮----
value = value.strip()
⋮----
def validate_inputs(payload)
⋮----
support_email = _required_string(payload, "supportEmail")
⋮----
privacy_url = _required_string(payload, "privacyPolicyUrl")
parsed = urlparse(privacy_url)
⋮----
admob = payload.get("admob")
⋮----
app_id = _required_string(admob, "appId")
interstitial_id = _required_string(admob, "interstitialId")
⋮----
fingerprint = _required_string(payload, "playAppSigningSha256").upper()
⋮----
billing = payload.get("billing")
product = billing.get("remove_ads_lifetime") if isinstance(billing, dict) else None
⋮----
tester_group = _required_string(payload, "testerGroup")
⋮----
def render_assetlinks(values)
⋮----
def render_gradle_properties(values)
⋮----
def render_privacy_policy(template, support_email)
⋮----
def render_privacy_html(template, support_email)
⋮----
rendered = template.replace(
⋮----
def render_summary(values)
⋮----
def generate(input_path, repo_root, output_dir)
⋮----
payload = json.loads(Path(input_path).read_text(encoding="utf-8"))
values = validate_inputs(payload)
repo = Path(repo_root)
markdown_template = (repo / "docs/privacy-policy.md").read_text(encoding="utf-8")
html_template = (repo / "docs/privacy/index.html").read_text(encoding="utf-8")
out = Path(output_dir)
⋮----
def main()
⋮----
parser = argparse.ArgumentParser(description="Validate external Play inputs and generate the production submission pack.")
⋮----
args = parser.parse_args()
⋮----
values = generate(args.input, args.repo_root, args.output)
```

## File: test_accessibility_launch_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class AccessibilityLaunchContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_profile_dashboard_has_button_role(self)
⋮----
def test_library_filters_expose_button_and_selection_semantics(self)
⋮----
def test_shared_pressable_surface_exposes_enabled_semantics(self)
```

## File: test_accessibility_system_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class AccessibilitySystemContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_shared_click_targets_are_at_least_52dp(self)
⋮----
def test_supporting_text_baseline_is_not_tiny(self)
⋮----
def test_library_filters_have_48dp_target(self)
⋮----
def test_library_result_content_is_not_forced_to_two_lines(self)
⋮----
block = self.library.split('private fun LibraryResultCard', 1)[1]
⋮----
def test_library_title_is_exposed_as_heading(self)
⋮----
title_block = self.library.split(
```

## File: test_actionable_knowledge_map_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ActionableKnowledgeMapContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_traits_are_clickable(self)
⋮----
def test_profile_opens_exploration_dialog(self)
⋮----
def test_dialog_exposes_evidence_and_next_measurement(self)
⋮----
def test_profile_can_navigate_directly_to_quiz(self)
```

## File: test_android_ci_sdk_setup_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
WORKFLOW_PATHS = (
⋮----
class AndroidCiSdkSetupContractTest(unittest.TestCase)
⋮----
def test_setup_android_skips_removed_legacy_tools_package(self)
⋮----
workflow = path.read_text(encoding='utf-8')
setup_block = workflow.split(
```

## File: test_baseline_profile_manifest_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
MANIFEST = (ROOT / 'baseline-profile/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
BUILD_FILE = (ROOT / 'baseline-profile/build.gradle.kts').read_text(encoding='utf-8')
⋮----
class BaselineProfileManifestContractTest(unittest.TestCase)
⋮----
def test_min_sdk_is_owned_by_gradle_not_manifest(self)
```

## File: test_battery_thermal_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class BatteryThermalContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_power_saver_disables_decorative_motion(self)
⋮----
def test_compact_quiz_artwork_is_static(self)
⋮----
def test_discover_idle_has_no_infinite_animation(self)
⋮----
discover = (ROOT / 'app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt').read_text(encoding='utf-8')
⋮----
def test_ads_are_not_preloaded_at_consent_startup(self)
⋮----
start_block = self.ads.split('fun start(activity: Activity?)', 1)[1].split('fun showPrivacyOptions', 1)[0]
⋮----
def test_interstitial_preload_waits_until_near_frequency_threshold(self)
⋮----
dismissed = self.ads.split('override fun onAdDismissedFullScreenContent()', 1)[1].split('}', 1)[0]
```

## File: test_behavior_copy_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
RESOURCE_FILES = [
⋮----
FORBIDDEN = {
⋮----
class BehaviorCopyContractTest(unittest.TestCase)
⋮----
def test_habits_copy_stays_non_clinical_and_non_moralizing(self)
⋮----
source = path.read_text(encoding="utf-8")
habits_copy = " ".join(
```

## File: test_behavior_goal_copy_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
EN = ROOT / "app/src/main/res/values/strings.xml"
FR = ROOT / "app/src/main/res/values-fr/strings.xml"
⋮----
REQUIRED = {
⋮----
FORBIDDEN = {
⋮----
def strings(path: Path)
⋮----
source = path.read_text(encoding="utf-8")
⋮----
class BehaviorGoalCopyContractTest(unittest.TestCase)
⋮----
def test_goal_copy_has_en_fr_parity(self)
⋮----
en = strings(EN)
fr = strings(FR)
⋮----
def test_goal_copy_states_targets_as_user_defined(self)
⋮----
def test_full_habits_reset_explicitly_mentions_goals(self)
⋮----
def test_goal_copy_stays_non_clinical_and_non_moralizing(self)
⋮----
values = strings(path)
goal_copy = " ".join(
```

## File: test_behavior_goal_privacy_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
ANDROID_NS = "http://schemas.android.com/apk/res/android"
⋮----
GOAL_TOKENS = [
⋮----
GOAL_FILES = [
⋮----
class BehaviorGoalPrivacyContractTest(unittest.TestCase)
⋮----
def read(self, name: str) -> str
⋮----
def test_goal_data_does_not_enter_telemetry_share_ads_or_personal_model(self)
⋮----
source = self.read(name)
⋮----
def test_goal_domain_does_not_write_profile_or_external_services(self)
⋮----
forbidden = [
⋮----
def test_goal_feature_declares_no_new_android_permission(self)
⋮----
root = ET.fromstring(MANIFEST.read_text(encoding="utf-8"))
declared = {
```

## File: test_behavior_goal_storage_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"
⋮----
class BehaviorGoalStorageContractTest(unittest.TestCase)
⋮----
def test_goal_repository_uses_separate_local_datastore(self)
⋮----
path = APP / "BehaviorGoalRepository.kt"
⋮----
source = path.read_text(encoding="utf-8")
⋮----
def test_goal_repository_does_not_write_profile_or_behavior_history(self)
⋮----
source = (APP / "BehaviorGoalRepository.kt").read_text(encoding="utf-8")
```

## File: test_behavior_goal_v1_scope_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"
EN = ROOT / "app/src/main/res/values/strings.xml"
FR = ROOT / "app/src/main/res/values-fr/strings.xml"
⋮----
class BehaviorGoalV1ScopeContractTest(unittest.TestCase)
⋮----
def read(self, name: str) -> str
⋮----
def test_v1_has_no_goal_editing_surface(self)
⋮----
integration = self.read("BehaviorGoalIntegrationUi.kt")
ui = self.read("BehaviorGoalsUi.kt")
⋮----
def test_v1_resources_do_not_expose_goal_editing_copy(self)
⋮----
source = path.read_text(encoding="utf-8")
⋮----
def test_full_habits_reset_clears_behavior_and_goal_data(self)
⋮----
main = self.read("MainActivity.kt")
```

## File: test_behavior_integration_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"
⋮----
class BehaviorIntegrationContractTest(unittest.TestCase)
⋮----
def read(self, name: str) -> str
⋮----
def test_profile_exposes_habits_entry(self)
⋮----
profile = self.read("ProfileScreenUi.kt")
⋮----
def test_main_activity_connects_profile_to_habits(self)
⋮----
source = self.read("MainActivity.kt")
⋮----
def test_habits_back_destination_is_profile(self)
⋮----
source = self.read("AppNavigation.kt")
⋮----
def test_behavior_refreshes_on_app_resume(self)
⋮----
source = self.read("BehaviorLifecycleUi.kt")
⋮----
main = self.read("MainActivity.kt")
```

## File: test_behavior_privacy_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"
⋮----
class BehaviorPrivacyContractTest(unittest.TestCase)
⋮----
def read(self, name: str) -> str
⋮----
def test_behavior_measurements_do_not_enter_telemetry(self)
⋮----
source = self.read("AppEvents.kt")
forbidden = [
⋮----
def test_behavior_measurements_do_not_enter_profile_share(self)
⋮----
source = self.read("GlobalProfileShare.kt")
⋮----
def test_behavior_measurements_do_not_enter_ad_configuration(self)
⋮----
source = self.read("AdManager.kt")
⋮----
def test_personal_model_engine_has_no_behavior_dependency(self)
⋮----
source = self.read("PersonalModelEngine.kt")
⋮----
def test_behavior_domain_does_not_write_profile_store_or_personal_model(self)
⋮----
behavior_files = [
⋮----
source = self.read(name)
```

## File: test_billing_launch_readiness.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class BillingLaunchReadinessTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_no_hard_coded_purchase_price_remains(self)
⋮----
def test_purchase_button_requires_live_play_price(self)
⋮----
def test_transient_product_query_failures_retry_boundedly(self)
⋮----
def test_new_manager_resets_price_to_loading(self)
```

## File: test_challenge_landing_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ChallengeLandingContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_landing_does_not_auto_launch_custom_scheme(self)
⋮----
def test_invalid_challenge_hides_open_button(self)
⋮----
def test_keyboard_focus_is_visible(self)
⋮----
def test_app_and_web_routes_stay_aligned(self)
```

## File: test_gradle_release_reproducibility.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class GradleReleaseReproducibilityContractTest(unittest.TestCase)
⋮----
def test_all_android_workflows_pin_same_gradle_version(self)
⋮----
workflows = [
versions = []
⋮----
source = path.read_text(encoding="utf-8")
match = re.search(r"gradle-version:\s*'([^']+)'", source)
⋮----
def test_build_scripts_pin_android_and_compose_plugins(self)
⋮----
root = (ROOT / "build.gradle.kts").read_text(encoding="utf-8")
```

## File: test_healthy_discover_profile_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class HealthyDiscoverProfileContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_discover_exploration_precedes_retention_metrics(self)
⋮----
def test_profile_social_actions_are_optional_and_not_accent_primary(self)
⋮----
share = self.profile.index('AppEvents.profileShare')
nearby = self.profile[share:share + 900]
⋮----
def test_premium_promotion_disappears_after_purchase(self)
⋮----
def test_optional_profile_copy_is_localized(self)
```

## File: test_large_screen_keyboard_accessibility.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class LargeScreenAndKeyboardAccessibilityTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_readable_width_is_centered_and_bounded(self)
⋮----
def test_keyboard_focus_is_visible_on_shell_and_filters(self)
⋮----
def test_accent_foregrounds_use_dark_ink_for_contrast(self)
⋮----
def test_catalog_failure_can_scroll_with_large_fonts(self)
⋮----
catalog_block = self.entry.split('fun CatalogUnavailableScreen()', 1)[1].split('@Composable\nfun OnboardingScreen', 1)[0]
```

## File: test_local_profile_privacy_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class LocalProfilePrivacyContractTest(unittest.TestCase)
⋮----
def test_android_backup_is_disabled_for_local_profile(self)
⋮----
manifest = (ROOT / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
```

## File: test_localization_parity.py
```python
ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"
⋮----
FORMAT = re.compile(r"%(?:\d+\$)?[a-zA-Z]")
⋮----
def strings_for(folder)
⋮----
values = {}
⋮----
root = ET.parse(path).getroot()
⋮----
name = node.attrib.get("name")
⋮----
text = "".join(node.itertext())
⋮----
class LocalizationParityContractTest(unittest.TestCase)
⋮----
def test_french_covers_all_translatable_english_strings(self)
⋮----
en = strings_for("values")
fr = strings_for("values-fr")
missing = sorted(set(en) - set(fr))
⋮----
def test_format_placeholders_match_between_locales(self)
⋮----
mismatches = []
⋮----
en_tokens = sorted(FORMAT.findall(en[name][0]))
fr_tokens = sorted(FORMAT.findall(fr[name][0]))
```

## File: test_longitudinal_trend_integration.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class LongitudinalTrendIntegrationContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_dated_history_is_persisted(self)
⋮----
def test_global_profile_carries_longitudinal_trends(self)
⋮----
def test_profile_renders_accessible_sparklines(self)
⋮----
def test_engine_distinguishes_outlier_volatility_and_direction(self)
```

## File: test_m33_readiness.py
```python
class M33ReadinessTest(unittest.TestCase)
⋮----
def test_manifest_matches_locked_release(self)
⋮----
data = json.loads(pathlib.Path('docs/m33-readiness.json').read_text(encoding='utf-8'))
```

## File: test_m46_accessibility.py
```python
ROOT = pathlib.Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"
⋮----
class CoreAccessibilityContractTest(unittest.TestCase)
⋮----
def read(self, filename: str) -> str
⋮----
def test_back_action_has_minimum_touch_target(self)
⋮----
source = self.read("AccessibilityUi.kt")
⋮----
def test_quiz_is_scrollable_and_answers_are_buttons(self)
⋮----
quiz = self.read("QuizScreenUi.kt")
pressable = self.read("V2InteractiveUi.kt")
⋮----
def test_shell_tabs_expose_tab_semantics(self)
⋮----
source = self.read("AppShellUi.kt")
⋮----
def test_result_secondary_actions_keep_touch_targets(self)
⋮----
source = self.read("ResultScreenUi.kt")
⋮----
def test_profile_progress_is_resource_backed(self)
⋮----
source = self.read("ProfileScreenUi.kt")
```

## File: test_m47_ui_test_wiring.py
```python
ROOT = pathlib.Path(__file__).resolve().parents[1]
APP_GRADLE = ROOT / "app" / "build.gradle.kts"
MAIN_RC = ROOT / ".github" / "workflows" / "m56-main-rc.yml"
ANDROID_TEST = ROOT / "app" / "src" / "androidTest" / "java" / "com" / "whoareyou" / "app"
⋮----
class M47UiTestWiringTest(unittest.TestCase)
⋮----
def test_compose_ui_test_dependencies_and_runner_stay_enabled(self)
⋮----
source = APP_GRADLE.read_text(encoding="utf-8")
⋮----
def test_github_actions_compiles_instrumentation_test_apk(self)
⋮----
source = MAIN_RC.read_text(encoding="utf-8")
⋮----
def test_critical_compose_ui_tests_exist(self)
```

## File: test_m48_visual_system.py
```python
ROOT = pathlib.Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"
⋮----
class M48VisualSystemTest(unittest.TestCase)
⋮----
def test_design_system_exposes_shared_type_and_motion_tokens(self)
⋮----
source = (APP / "V2DesignSystem.kt").read_text(encoding="utf-8")
⋮----
def test_quiz_uses_shared_pressable_surface_and_typography(self)
⋮----
source = (APP / "QuizScreenUi.kt").read_text(encoding="utf-8")
⋮----
def test_profile_and_result_use_shared_hero_typography(self)
⋮----
source = (APP / filename).read_text(encoding="utf-8")
⋮----
def test_press_feedback_is_animated_and_reusable(self)
⋮----
source = (APP / "V2InteractiveUi.kt").read_text(encoding="utf-8")
⋮----
def test_discover_core_uses_shared_typography(self)
⋮----
def test_discover_clickable_cards_use_press_feedback(self)
⋮----
def test_shell_has_animated_press_feedback(self)
⋮----
source = (APP / "AppShellUi.kt").read_text(encoding="utf-8")
```

## File: test_m49_architecture_cleanup.py
```python
ROOT = pathlib.Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"
⋮----
class M49ArchitectureCleanupTest(unittest.TestCase)
⋮----
def test_main_activity_is_orchestration_only(self)
⋮----
source = (APP / "MainActivity.kt").read_text(encoding="utf-8")
⋮----
def test_entry_screens_are_extracted(self)
⋮----
source = (APP / "EntryScreensUi.kt").read_text(encoding="utf-8")
⋮----
def test_challenge_activity_is_deep_link_orchestration_only(self)
⋮----
source = (APP / "ChallengeActivity.kt").read_text(encoding="utf-8")
⋮----
def test_challenge_ui_uses_shared_v2_system(self)
⋮----
source = (APP / "ChallengeUi.kt").read_text(encoding="utf-8")
```

## File: test_m50_analytics.py
```python
ROOT = pathlib.Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"
⋮----
class M50AnalyticsTest(unittest.TestCase)
⋮----
def test_core_funnel_is_explicit(self)
⋮----
source = (APP / "AppEvents.kt").read_text(encoding="utf-8")
⋮----
def test_behavioral_scores_are_bucketed(self)
⋮----
def test_remote_errors_do_not_send_message_or_stack(self)
⋮----
source = (APP / "Telemetry.kt").read_text(encoding="utf-8")
http_section = source.split("internal class HttpEventSink", 1)[1]
⋮----
def test_main_app_wires_activation_and_abandonment(self)
⋮----
main_source = (APP / "MainActivity.kt").read_text(encoding="utf-8")
result_commit_source = (APP / "QuizResultCommitEffect.kt").read_text(encoding="utf-8")
⋮----
def test_billing_wires_conversion_steps(self)
⋮----
source = (APP / "BillingManager.kt").read_text(encoding="utf-8")
```

## File: test_m51_release_readiness.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class M51ReleaseReadinessTest(unittest.TestCase)
⋮----
def test_release_build_is_optimized(self)
⋮----
gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
⋮----
def test_signing_material_is_never_committed(self)
⋮----
gitignore = (ROOT / ".gitignore").read_text(encoding="utf-8")
⋮----
def test_manifest_uses_production_safe_defaults(self)
⋮----
manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
⋮----
def test_github_actions_proves_release_variant(self)
⋮----
config = (ROOT / ".github/workflows/m56-main-rc.yml").read_text(encoding="utf-8")
⋮----
def test_protected_play_workflows_exist(self)
⋮----
candidate = (ROOT / ".github/workflows/play-candidate.yml").read_text(encoding="utf-8")
publish = (ROOT / ".github/workflows/play-internal-publish.yml").read_text(encoding="utf-8")
```

## File: test_m52_play_policy.py
```python
ROOT = pathlib.Path(__file__).resolve().parents[1]
⋮----
class M52PlayPolicyTests(unittest.TestCase)
⋮----
def read(self, path: str) -> str
⋮----
def test_ump_refreshes_and_exposes_privacy_options(self)
⋮----
ad = self.read("app/src/main/java/com/whoareyou/app/AdManager.kt")
⋮----
def test_privacy_entry_point_is_user_visible_when_required(self)
⋮----
main = self.read("app/src/main/java/com/whoareyou/app/MainActivity.kt")
hub = self.read("app/src/main/java/com/whoareyou/app/DiscoverHubUi.kt")
en = self.read("app/src/main/res/values/strings.xml")
fr = self.read("app/src/main/res/values-fr/strings.xml")
⋮----
def test_manifest_is_minimal_and_hardened(self)
⋮----
manifest = self.read("app/src/main/AndroidManifest.xml")
⋮----
def test_file_provider_exports_only_shared_result_cache(self)
⋮----
paths = self.read("app/src/main/res/xml/file_paths.xml")
⋮----
def test_billing_uses_play_billing_and_acknowledges_purchases(self)
⋮----
billing = self.read("app/src/main/java/com/whoareyou/app/BillingManager.kt")
⋮----
def test_https_app_link_is_scoped_to_challenges(self)
```

## File: test_m53_publication_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class M53PublicationContractTest(unittest.TestCase)
⋮----
def test_privacy_page_is_publication_ready(self)
⋮----
html = (ROOT / "docs/privacy/index.html").read_text(encoding="utf-8")
⋮----
def test_data_safety_tracks_current_admob_disclosures(self)
⋮----
text = (ROOT / "docs/play-data-safety.md").read_text(encoding="utf-8")
⋮----
def test_public_release_inputs_contract_is_safe(self)
⋮----
payload = json.loads(
⋮----
def test_strict_public_release_gate_rejects_template_placeholders(self)
⋮----
errors = validate(payload, strict=True)
⋮----
def test_app_links_docs_explain_root_domain_dependency(self)
⋮----
text = (ROOT / "docs/APP_LINKS.md").read_text(encoding="utf-8")
```

## File: test_m54_play_preflight.py
```python
class M54PlayPreflightTest(unittest.TestCase)
⋮----
def test_repository_preflight_is_ready_without_external_inputs(self)
⋮----
report = run(ROOT, None, strict_public=False)
⋮----
def test_strict_public_preflight_requires_external_inputs(self)
⋮----
report = run(ROOT, None, strict_public=True)
⋮----
def test_strict_public_preflight_rejects_template_placeholders(self)
⋮----
template = ROOT / "docs/public-release-inputs.template.json"
report = run(ROOT, template, strict_public=True)
⋮----
def test_strict_public_preflight_accepts_complete_contract_shape(self)
⋮----
payload = {
⋮----
path = Path(temp) / "inputs.json"
⋮----
report = run(ROOT, path, strict_public=True)
```

## File: test_m55_release_automation.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class M55ReleaseAutomationTest(unittest.TestCase)
⋮----
def test_publisher_uses_current_internal_track_and_production_guard(self)
⋮----
text = (ROOT / "tools/play_publisher.py").read_text(encoding="utf-8")
⋮----
def test_promotion_helper_exists_without_bundle_upload(self)
⋮----
text = (ROOT / "tools/play_promoter.py").read_text(encoding="utf-8")
⋮----
def test_promotion_workflow_is_manual_and_safe_by_default(self)
⋮----
text = (ROOT / ".github/workflows/play-promote.yml").read_text(encoding="utf-8")
⋮----
def test_public_preflight_remains_available(self)
⋮----
text = (ROOT / "tools/play_preflight.py").read_text(encoding="utf-8")
```

## File: test_m56_release_candidate.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class M56ReleaseCandidateTest(unittest.TestCase)
⋮----
def test_manifest_has_launcher_identity(self)
⋮----
manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
⋮----
def test_adaptive_and_monochrome_icons_exist(self)
⋮----
required = (
⋮----
icon = (ROOT / path).read_text(encoding="utf-8")
⋮----
themed = (ROOT / "app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml").read_text(encoding="utf-8")
⋮----
def test_candidate_build_is_release_like_and_installable(self)
⋮----
gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
⋮----
def test_github_actions_builds_and_verifies_installable_apk(self)
⋮----
config = (ROOT / ".github/workflows/m56-main-rc.yml").read_text(encoding="utf-8")
⋮----
version_name = re.search(r'versionName\s*=\s*"([^"]+)"', gradle)
version_code = re.search(r'versionCode\s*=\s*(\d+)', gradle)
⋮----
version_name = version_name.group(1)
version_code = version_code.group(1)
⋮----
def test_circleci_is_lightweight_contract_gate(self)
⋮----
circleci = (ROOT / ".circleci/config.yml").read_text(encoding="utf-8")
```

## File: test_m59_host_diagnostics_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github" / "scripts" / "android-device-validation.sh"
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"
⋮----
class M59HostDiagnosticsContractTest(unittest.TestCase)
⋮----
def test_device_validation_captures_host_resource_pressure_during_instrumentation(self)
⋮----
script = SCRIPT.read_text(encoding="utf-8")
⋮----
def test_device_validation_uploads_host_diagnostics(self)
⋮----
workflow = WORKFLOW.read_text(encoding="utf-8")
```

## File: test_m59_no_kvm_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"
⋮----
def visual_job(workflow: str) -> str
⋮----
class M59HardwareAccelerationContractTest(unittest.TestCase)
⋮----
def test_visual_validation_keeps_linux_hardware_acceleration(self)
⋮----
workflow = WORKFLOW.read_text(encoding="utf-8")
visual = visual_job(workflow)
⋮----
# M764 proved -accel off takes ~13 minutes to boot and then loses ADB
# before validation starts. The split visual job must keep KVM enabled.
```

## File: test_m59_runtime_stress_split_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"
DEVICE_SCRIPT = ROOT / ".github" / "scripts" / "android-device-validation.sh"
STRESS_SCRIPT = ROOT / ".github" / "scripts" / "android-runtime-stress.sh"
⋮----
class M59RuntimeStressSplitContractTest(unittest.TestCase)
⋮----
def test_atd_job_owns_runtime_stress(self)
⋮----
workflow = WORKFLOW.read_text(encoding="utf-8")
⋮----
def test_visual_validation_skips_random_monkey_stress(self)
⋮----
script = DEVICE_SCRIPT.read_text(encoding="utf-8")
⋮----
def test_runtime_stress_script_preserves_debug_and_candidate_coverage(self)
⋮----
script = STRESS_SCRIPT.read_text(encoding="utf-8")
⋮----
def test_runtime_stress_captures_startup_evidence_before_pid_assertion(self)
⋮----
start = script.index('start_output="$(adb shell am start -W -n "$ACTIVITY")"')
first_capture = script.index('capture_runtime_evidence "$label"', start)
pid_lookup = script.index('pid="$(adb shell pidof', start)
```

## File: test_m59_split_validation_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"
SCRIPT = ROOT / ".github" / "scripts" / "android-device-validation.sh"
⋮----
def job_block(workflow: str, job_name: str, next_job_name: str | None = None) -> str
⋮----
marker = f"  {job_name}:"
self_start = workflow.index(marker)
block = workflow[self_start:]
⋮----
next_marker = f"  {next_job_name}:"
block = block[: block.index(next_marker)]
⋮----
class M59SplitValidationContractTest(unittest.TestCase)
⋮----
def test_instrumentation_and_visual_validation_use_separate_emulators(self)
⋮----
workflow = WORKFLOW.read_text(encoding="utf-8")
instrumentation = job_block(workflow, "instrumentation_validation", "visual_validation")
visual = job_block(workflow, "visual_validation")
⋮----
def test_visual_mode_skips_connected_instrumentation(self)
⋮----
script = SCRIPT.read_text(encoding="utf-8")
```

## File: test_m59_visual_emulator_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "m59-device-validation.yml"
⋮----
def visual_job(workflow: str) -> str
⋮----
class M59VisualEmulatorContractTest(unittest.TestCase)
⋮----
def test_visual_validation_uses_rendering_capable_system_image(self)
⋮----
workflow = WORKFLOW.read_text(encoding="utf-8")
visual = visual_job(workflow)
⋮----
# Automated Test Device images disable hardware rendering and therefore
# cannot be used as the source of screenshot-based visual evidence.
⋮----
def test_visual_validation_uses_supported_software_renderer(self)
⋮----
# Emulator 36.4.9 deprecated swiftshader_indirect. Keep the visual gate
# on a supported software renderer rather than the legacy indirect backend.
⋮----
def test_visual_validation_pins_known_stable_emulator_build(self)
⋮----
# Keep rendered evidence on the final stable 36.x patch while the ATD
# job independently owns connected instrumentation coverage.
```

## File: test_m769_landscape_viewport.py
```python
ROOT = Path(__file__).resolve().parents[1]
VALIDATOR = ROOT / ".github" / "scripts" / "validate-ui-hierarchy.py"
⋮----
def _write_png_header(path: Path, width: int, height: int) -> None
⋮----
signature = b"\x89PNG\r\n\x1a\n"
ihdr = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
⋮----
class LandscapeViewportValidationTest(unittest.TestCase)
⋮----
def test_landscape_viewport_comes_from_rendered_screenshot(self) -> None
⋮----
tmp_path = Path(tmp)
screenshot = tmp_path / "landscape.png"
hierarchy = tmp_path / "landscape.xml"
⋮----
result = subprocess.run(
```

## File: test_m771_localization.py
```python
ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app" / "ResultIntelligenceUi.kt"
EN = ROOT / "app" / "src" / "main" / "res" / "values" / "strings.xml"
FR = ROOT / "app" / "src" / "main" / "res" / "values-fr" / "strings.xml"
⋮----
KEYS = {
⋮----
def keys(path)
⋮----
root = ET.parse(path).getroot()
⋮----
class M771LocalizationTest(unittest.TestCase)
⋮----
def test_m771_ui_copy_is_resource_backed_and_bilingual(self)
⋮----
source = UI.read_text(encoding="utf-8")
en = keys(EN)
fr = keys(FR)
```

## File: test_m771_result_intelligence_catalog.py
```python
ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
PAIRS = [
⋮----
REQUIRED_BRANCHES = ("low", "balanced", "high")
REQUIRED_FIELDS = ("strengths", "watchOuts", "everydayLife", "reflection")
⋮----
def load(path)
⋮----
class ResultIntelligenceCatalogTest(unittest.TestCase)
⋮----
def test_all_quizzes_have_complete_result_intelligence_with_en_fr_parity(self)
⋮----
en = {q["id"]: q for q in load(ASSETS / en_name)}
fr = {q["id"]: q for q in load(ASSETS / fr_name)}
⋮----
intelligence = quiz.get("resultIntelligence")
⋮----
payload = intelligence[branch]
```

## File: test_main_activity_architecture.py
```python
ROOT = pathlib.Path(__file__).resolve().parents[1]
APP_PACKAGE = ROOT / "app" / "src" / "main" / "java" / "com" / "whoareyou" / "app"
MAIN_ACTIVITY = APP_PACKAGE / "MainActivity.kt"
⋮----
class MainActivityArchitectureTest(unittest.TestCase)
⋮----
def test_main_activity_stays_a_lightweight_shell(self)
⋮----
source = MAIN_ACTIVITY.read_text(encoding="utf-8")
⋮----
def test_shell_routes_to_extracted_screens(self)
⋮----
def test_extracted_screen_files_exist(self)
```

## File: test_main_activity_recreation_sync_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
SOURCE = (
⋮----
class MainActivityRecreationSyncContractTest(unittest.TestCase)
⋮----
def test_recreation_test_uses_v2_compose_rule(self)
⋮----
def test_recreation_test_waits_for_discover_before_scrolling(self)
⋮----
test_body = SOURCE.split(
wait_index = test_body.index('waitForTag("app_screen_discover")')
scroll_index = test_body.index('onNodeWithTag("discover_list")')
```

## File: test_main_flow_recommendation_parity_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
TEST_SOURCE = (
⋮----
class MainFlowRecommendationParityContractTest(unittest.TestCase)
⋮----
def test_e2e_recommendation_uses_same_profile_coverage_as_discover_ui(self)
```

## File: test_manifest_security_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
ANDROID_NS = "http://schemas.android.com/apk/res/android"
⋮----
class ManifestSecurityContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_manifest_uses_only_approved_permissions_and_secure_defaults(self)
⋮----
approved = {
root = ET.fromstring(self.manifest)
declared = {
⋮----
def test_file_provider_is_not_exported_and_paths_are_narrow(self)
⋮----
def test_billing_restore_revokes_missing_entitlement(self)
⋮----
def test_top_level_compose_surfaces_handle_status_bar_insets(self)
⋮----
paths = [
⋮----
source = (
⋮----
def test_ad_show_failure_preloads_replacement(self)
⋮----
marker = "override fun onAdFailedToShowFullScreenContent"
block = self.ads[self.ads.index(marker):]
block = block[:block.index("override fun onAdImpression")]
```

## File: test_offline_resilience_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class OfflineResilienceContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_http_telemetry_queue_is_bounded(self)
⋮----
def test_disabled_production_telemetry_is_noop(self)
⋮----
def test_share_launcher_handles_non_activity_contexts_and_failures(self)
```

## File: test_performance_release_gate_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class PerformanceReleaseGateContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_baseline_profile_toolchain_is_wired(self)
⋮----
def test_benchmark_covers_startup_and_requires_baseline_profile(self)
⋮----
def test_play_preflight_compiles_benchmark_variants(self)
⋮----
expected = "gradle :app:assembleBenchmark :baseline-profile:assembleBenchmark --stacktrace"
⋮----
def test_play_artifacts_include_bundle_budget(self)
⋮----
def test_production_promotion_requires_vitals_and_prelaunch_review(self)
```

## File: test_persisted_result_score_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class PersistedResultScoreContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_result_callback_uses_persisted_profile_score(self)
⋮----
def test_main_activity_sets_final_score_only_after_commit_callback(self)
⋮----
finish_block = self.main.split("onFinished = { score ->", 1)[1].split("}", 2)[0]
```

## File: test_play_2026_readiness.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class Play2026ReadinessContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_target_sdk_meets_august_2026_play_requirement(self)
⋮----
match = re.search(r'targetSdk\s*=\s*(\d+)', self.gradle)
⋮----
def test_compile_sdk_is_not_below_target_sdk(self)
⋮----
compile_sdk = int(re.search(r'compileSdk\s*=\s*(\d+)', self.gradle).group(1))
target_sdk = int(re.search(r'targetSdk\s*=\s*(\d+)', self.gradle).group(1))
⋮----
def test_play_candidate_runs_quality_preflight_before_signing(self)
⋮----
build_index = self.workflow.index('Build signed Play candidate')
⋮----
def test_play_candidate_installs_declared_android_sdk(self)
⋮----
def test_play_artifact_reports_native_libraries(self)
```

## File: test_play_bundle_budget.py
```python
class PlayBundleBudgetTest(unittest.TestCase)
⋮----
def test_inspection_reports_bundle_structure(self)
⋮----
path = Path(tmp) / "sample.aab"
⋮----
report = budget.inspect_bundle(path)
⋮----
def test_internal_defaults_are_explicit_and_ordered(self)
```

## File: test_play_candidate_workflow.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class PlayCandidateWorkflowTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_manual_only_and_read_only(self)
⋮----
def test_required_secrets_are_referenced(self)
⋮----
def test_optional_secrets_are_referenced(self)
⋮----
def test_upload_key_password_falls_back_to_keystore_password(self)
⋮----
def test_keystore_is_temporary_and_cleaned(self)
⋮----
def test_signed_candidate_is_verified_and_uploaded(self)
⋮----
gradle = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
version_name = re.search(r'versionName\s*=\s*"([^"]+)"', gradle).group(1)
version_code = re.search(r'versionCode\s*=\s*(\d+)', gradle).group(1)
artifact_name = self.contract['artifactNameTemplate'].format(
artifact_files = [
⋮----
def test_contract_does_not_pin_a_specific_app_version(self)
⋮----
serialized = json.dumps(self.contract)
⋮----
def test_workflow_does_not_upload_to_play(self)
⋮----
lowered = self.workflow.lower()
```

## File: test_play_promoter.py
```python
class PlayPromotionPayloadTest(unittest.TestCase)
⋮----
def test_internal_track_name_matches_current_play_api_contract(self)
⋮----
def test_closed_track_can_be_draft(self)
⋮----
payload = play_publisher.release_payload(track="closed-test", status="draft")
⋮----
def test_open_track_can_complete(self)
⋮----
payload = play_publisher.release_payload(track="beta", status="completed")
⋮----
def test_production_needs_confirmation(self)
⋮----
def test_staged_production_payload(self)
⋮----
payload = play_publisher.release_payload(
release = payload["releases"][0]
```

## File: test_play_publish_pipeline_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class PlayPublishPipelineContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_internal_publish_runs_quality_gates_before_bundle(self)
⋮----
build = self.internal.index('Build signed production Play bundle')
⋮----
def test_internal_publish_uses_dynamic_versioned_receipt(self)
⋮----
def test_internal_publish_uses_optional_key_password_fallback(self)
⋮----
def test_promotion_receipt_is_not_pinned_to_old_version(self)
⋮----
def test_project_proguard_rules_do_not_keep_unused_room_or_workmanager(self)
```

## File: test_play_publish_workflow.py
```python
class PlayPublishWorkflowContractTest(unittest.TestCase)
⋮----
@classmethod
    def setUpClass(cls)
⋮----
def test_manual_only_and_read_only(self)
⋮----
def test_publish_is_opt_in(self)
⋮----
def test_internal_track_publisher_and_service_account_secret(self)
⋮----
def test_build_uses_strict_play_release_and_signature_verification(self)
⋮----
def test_actual_upload_commits_edit_only_in_publish_job(self)
⋮----
def test_temporary_credentials_are_cleaned(self)
```

## File: test_play_publisher.py
```python
class PlayPublisherTest(unittest.TestCase)
⋮----
def test_locked_release_config(self)
⋮----
config = play_publisher.ReleaseConfig()
⋮----
def test_default_release_payload_uses_internal_qa_track(self)
⋮----
payload = play_publisher.release_payload()
⋮----
release = payload["releases"][0]
⋮----
def test_open_testing_track_is_supported(self)
⋮----
payload = play_publisher.release_payload(track="beta", status="completed")
⋮----
def test_custom_closed_track_is_supported(self)
⋮----
payload = play_publisher.release_payload(track="closed-alpha", status="draft")
⋮----
def test_production_requires_explicit_confirmation(self)
⋮----
def test_production_staged_rollout_is_supported_when_confirmed(self)
⋮----
payload = play_publisher.release_payload(
⋮----
def test_production_in_progress_requires_fraction(self)
⋮----
def test_non_production_rollout_fraction_is_rejected(self)
⋮----
def test_invalid_status_is_rejected(self)
⋮----
def test_only_locked_package_is_allowed(self)
⋮----
def test_malformed_track_is_rejected(self)
⋮----
def test_service_account_validation_rejects_missing_fields(self)
⋮----
path = pathlib.Path(directory) / "service-account.json"
⋮----
def test_endpoint_shape(self)
```

## File: test_play_release_safety_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class PlayReleaseSafetyContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_upload_key_password_can_fall_back_to_keystore_password(self)
⋮----
def test_standard_release_disables_external_services(self)
⋮----
release_block = self.gradle.split("release {", 1)[1].split("}", 1)[0]
⋮----
def test_play_release_explicitly_reenables_external_services(self)
⋮----
play_block = self.gradle.split('create("playRelease") {', 1)[1]
```

## File: test_prepare_play_submission.py
```python
VALID = {
⋮----
class PlaySubmissionGeneratorTest(unittest.TestCase)
⋮----
def test_valid_inputs_generate_all_files(self)
⋮----
root = Path(temp)
⋮----
input_path = root / "input.json"
⋮----
out = root / "out"
values = generate(input_path, root, out)
⋮----
final_html = (out / "privacy-policy-final.html").read_text()
⋮----
def test_placeholder_is_rejected(self)
⋮----
payload = dict(VALID)
⋮----
def test_non_https_privacy_url_is_rejected(self)
⋮----
def test_malformed_admob_id_is_rejected(self)
⋮----
payload = json.loads(json.dumps(VALID))
⋮----
def test_malformed_fingerprint_is_rejected(self)
⋮----
def test_inactive_billing_product_is_rejected(self)
```

## File: test_profile_coverage_integration.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ProfileCoverageIntegrationContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_global_profile_carries_coverage(self)
⋮----
def test_profile_surfaces_coverage_card(self)
⋮----
def test_recommendation_engine_targets_uncertainty(self)
⋮----
def test_coverage_model_tracks_confidence_and_contradictions(self)
```

## File: test_profile_knowledge_map_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ProfileKnowledgeMapContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_map_has_explicit_domains(self)
⋮----
def test_map_keeps_three_knowledge_states(self)
⋮----
def test_profile_renders_knowledge_map(self)
```

## File: test_profile_narrative_integration.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ProfileNarrativeIntegrationContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_global_profile_contains_narrative(self)
⋮----
def test_narrative_separates_change_from_more_evidence(self)
⋮----
def test_uncertainty_blocks_overconfident_directional_copy(self)
⋮----
def test_profile_surfaces_factual_summary(self)
```

## File: test_profile_reset_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ProfileResetContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_store_exposes_bounded_profile_clear(self)
⋮----
def test_reset_requires_confirmation(self)
⋮----
def test_main_wires_reset_to_store(self)
⋮----
def test_copy_is_localized_and_scope_is_profile_specific(self)
```

## File: test_profile_result_allocation_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ProfileResultAllocationContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_profile_sort_and_catalog_index_are_remembered(self)
⋮----
def test_profile_and_result_background_gradients_are_remembered(self)
⋮----
def test_result_recommendation_uses_indexed_catalog_and_stable_brush(self)
⋮----
def test_identity_aura_uses_draw_cache_for_geometry(self)
```

## File: test_profile_startup_resilience.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ProfileStartupResilienceTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_default_profile_does_not_skip_onboarding(self)
⋮----
def test_profile_observation_recovers_with_empty_preferences(self)
```

## File: test_progression_journal_integration.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ProgressionJournalIntegrationContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_journal_keeps_dates_and_period_comparison(self)
⋮----
def test_profile_opens_journal_from_dimension(self)
⋮----
def test_journal_ui_formats_real_dates(self)
⋮----
def test_new_trait_evidence_keeps_source_quiz_ids(self)
```

## File: test_public_launch_experience_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class PublicLaunchExperienceContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_privacy_and_support_are_permanently_accessible(self)
⋮----
def test_profile_deletion_is_confirmed(self)
⋮----
def test_purchase_ui_never_invents_a_price(self)
⋮----
def test_onboarding_is_compact_screen_safe(self)
⋮----
def test_first_run_copy_states_local_profile_and_non_diagnostic_scope(self)
```

## File: test_public_result_and_store_positioning.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class PublicResultAndStorePositioningTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_done_is_primary_before_optional_social_actions(self)
⋮----
done_index = self.result.index('testTag("result_done")')
share_index = self.result.index("R.string.share_my_result")
compare_index = self.result.index("R.string.compare_with_friend")
⋮----
def test_completed_catalog_does_not_force_retake_recommendation(self)
⋮----
def test_store_positioning_leads_with_self_reflection(self)
⋮----
assets = (ROOT / "docs/store-assets-spec.md").read_text(encoding="utf-8")
⋮----
def test_play_console_sheet_is_version_agnostic(self)
```

## File: test_quiz_commit_interaction_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class QuizCommitInteractionContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_system_back_is_blocked_while_result_is_saving(self)
⋮----
def test_visible_back_action_does_not_leave_during_commit(self)
⋮----
def test_answers_are_semantically_disabled_during_commit(self)
```

## File: test_quiz_commit_interaction_lock.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class QuizCommitInteractionLockTest(unittest.TestCase)
⋮----
def test_quiz_screen_exposes_finishing_state(self)
⋮----
source = (ROOT / "app/src/main/java/com/whoareyou/app/QuizScreenUi.kt").read_text(encoding="utf-8")
⋮----
def test_main_activity_wires_commit_state_into_quiz_screen(self)
⋮----
source = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
```

## File: test_quiz_process_recreation_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class QuizProcessRecreationContractTest(unittest.TestCase)
⋮----
def test_critical_attempt_state_is_saveable(self)
⋮----
source = (
expected = (
⋮----
def test_pending_result_replays_through_idempotent_commit_effect(self)
```

## File: test_quiz_replay_window_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class QuizReplayWindowContractTest(unittest.TestCase)
⋮----
def test_replay_history_is_large_but_bounded(self)
⋮----
source = (
⋮----
def test_last_attempt_per_quiz_is_still_retained_for_migration_and_replay_protection(self)
```

## File: test_quiz_result_persistence_feedback.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class QuizResultPersistenceFeedbackTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_saving_and_failure_states_are_visible_and_localized(self)
⋮----
def test_commit_failure_preserves_retry_path(self)
⋮----
def test_new_finish_attempt_clears_previous_failure(self)
```

## File: test_reduced_motion_large_font_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ReducedMotionAndLargeFontContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_android_animation_scale_is_respected(self)
⋮----
def test_reduced_motion_uses_snap_for_micro_interactions(self)
⋮----
def test_editorial_cards_do_not_force_two_line_truncation(self)
⋮----
block = self.collections.split('private fun EditorialCard', 1)[1]
```

## File: test_release_ci_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/android-ci.yml"
⋮----
class ReleaseCiContractTest(unittest.TestCase)
⋮----
def test_ci_builds_release_like_candidate_apk(self)
⋮----
source = WORKFLOW.read_text(encoding="utf-8")
```

## File: test_release_critical_profile_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ReleaseCriticalProfileContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_profile_store_decodes_both_histories(self)
⋮----
update = self.store[self.store.index("val history = ScoreHistoryEngine.update("):]
update = update[:update.index("prefs[completedKey]")]
⋮----
def test_profile_quiz_navigation_uses_mutable_quiz_id(self)
⋮----
profile_branch = self.main[self.main.index("AppScreen.PROFILE -> ProfileScreen("):]
profile_branch = profile_branch[:profile_branch.index("AppScreen.QUIZ ->")]
⋮----
def test_retake_cadence_has_hard_minimum(self)
⋮----
def test_profile_explains_next_quiz(self)
```

## File: test_release_integration_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main/java/com/whoareyou/app"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
ANDROID_NS = "http://schemas.android.com/apk/res/android"
⋮----
class ReleaseIntegrationContractTest(unittest.TestCase)
⋮----
def read(self, name: str) -> str
⋮----
def test_fresh_behavior_state_is_disabled_until_user_action(self)
⋮----
main = self.read("MainActivity.kt")
⋮----
def test_permission_launches_are_confined_to_user_source_actions(self)
⋮----
def test_profile_and_habits_resets_have_separate_scopes(self)
⋮----
profile_reset = (
habits_reset = (
⋮----
def test_goal_progress_is_recomputed_not_persisted(self)
⋮----
repo = self.read("BehaviorGoalRepository.kt")
presentation = self.read("BehaviorGoalPresentation.kt")
integration = self.read("BehaviorGoalIntegrationUi.kt")
⋮----
def test_release_hardening_adds_no_permission(self)
⋮----
root = ET.fromstring(MANIFEST.read_text(encoding="utf-8"))
declared = {
⋮----
def test_behavior_and_goal_stores_remain_separate(self)
⋮----
behavior = self.read("BehaviorRepository.kt")
goals = self.read("BehaviorGoalRepository.kt")
```

## File: test_release_workflows_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ReleaseWorkflowContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_ci_runs_automatically(self)
⋮----
def test_internal_publish_has_single_release_tail(self)
⋮----
def test_internal_publish_verifies_bundle_and_budget(self)
⋮----
def test_circleci_is_fast_deterministic_contract_gate(self)
⋮----
def test_android_37_sdk_package_name_is_valid(self)
⋮----
workflows = (self.ci, self.candidate, self.internal)
⋮----
def test_candidate_and_internal_run_core_quality_gates(self)
⋮----
class ComposeImportContractTest(unittest.TestCase)
⋮----
def _assert_extension_import(self, token, import_line, label)
⋮----
root = ROOT / "app/src/main/java/com/whoareyou/app"
offenders = []
⋮----
source = path.read_text(encoding="utf-8")
⋮----
def test_padding_extension_has_import_when_used(self)
⋮----
def test_weight_uses_scope_extension_without_invalid_import(self)
⋮----
def test_height_extension_has_import_when_used(self)
```

## File: test_rendering_performance_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class RenderingPerformanceContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_macrobenchmark_measures_discover_and_quiz_result_frames(self)
⋮----
def test_discover_idle_has_no_perpetual_ambient_animation(self)
⋮----
def test_remaining_ambient_animation_state_is_read_in_graphics_layer(self)
⋮----
def test_result_share_no_longer_reparses_quiz_catalog(self)
```

## File: test_restored_quiz_recovery.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class RestoredQuizRecoveryTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_missing_restored_quiz_does_not_fall_back_with_stale_progress(self)
⋮----
def test_missing_quiz_resets_entire_attempt_before_returning_to_discover(self)
```

## File: test_result_visual_hierarchy_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ResultVisualHierarchyContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_score_card_has_stable_visual_anchor(self)
⋮----
def test_score_card_keeps_accessible_score_semantics(self)
```

## File: test_runtime_efficiency_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class RuntimeEfficiencyContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_quiz_catalog_cache_returns_before_asset_checks(self)
⋮----
load = self.catalog.split('fun load(context: Context)', 1)[1].split('fun find(', 1)[0]
cache_index = load.index('cached?.takeIf { cachedLanguage == language }?.let { return it }')
asset_index = load.index('assetExists(appContext, localizedName)')
⋮----
def test_premium_users_do_not_create_ad_manager(self)
⋮----
def test_discover_sections_use_stable_lazy_keys(self)
⋮----
def test_ad_manager_has_explicit_release_path(self)
```

## File: test_runtime_memory_lifecycle.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class RuntimeMemoryLifecycleContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_billing_uses_application_context_and_cancels_scope(self)
⋮----
def test_ads_use_application_context_for_long_lived_services(self)
⋮----
def test_http_telemetry_thread_can_time_out(self)
```

## File: test_runtime_performance_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class RuntimePerformanceContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_catalog_parsing_is_off_main_thread(self)
⋮----
def test_external_sdks_are_after_onboarding_and_catalog_guards(self)
⋮----
manager_index = self.main.index('val billingManager = remember(context)')
onboarding_index = self.main.index('if (!storedProfile.onboardingComplete)')
catalog_index = self.main.index('if (!AppNavigation.hasUsableCatalog')
⋮----
def test_billing_reconnects_are_bounded(self)
⋮----
def test_datastore_fallback_only_handles_io_errors(self)
⋮----
def test_reduced_motion_removes_ambient_frame_loops(self)
⋮----
def test_app_reports_first_useful_draw(self)
```

## File: test_share_rendering_performance.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ShareRenderingPerformanceContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_cpu_rendering_uses_default_dispatcher(self)
⋮----
def test_file_work_uses_io_dispatcher(self)
⋮----
def test_result_share_uses_known_quiz_id_instead_of_catalog_parse(self)
```

## File: test_share_storage_safety.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ShareStorageSafetyContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_share_cache_is_bounded_and_png_encoding_is_checked(self)
⋮----
def test_file_provider_exposes_only_share_subdirectory(self)
⋮----
def test_all_image_shares_use_shared_file_store(self)
```

## File: test_shell_accessibility_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class ShellAccessibilityContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_shell_respects_reduced_motion(self)
⋮----
def test_shell_height_can_expand_for_large_fonts(self)
⋮----
def test_back_action_uses_shared_accessible_type_scale(self)
```

## File: test_startup_loading_state.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class StartupLoadingStateTest(unittest.TestCase)
⋮----
def test_profile_loading_has_visible_testable_ui(self)
⋮----
source = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
⋮----
entry = (ROOT / "app/src/main/java/com/whoareyou/app/EntryScreensUi.kt").read_text(encoding="utf-8")
⋮----
def test_startup_does_not_return_silently_before_profile_load(self)
```

## File: test_string_resource_parity.py
```python
ROOT = Path(__file__).resolve().parents[1]
VALUES = ROOT / "app/src/main/res/values/strings.xml"
VALUES_FR = ROOT / "app/src/main/res/values-fr/strings.xml"
⋮----
NAME_RE = re.compile(r'<string\s+name="([^"]+)"')
⋮----
def resource_names(path: Path) -> set[str]
⋮----
class StringResourceParityTest(unittest.TestCase)
⋮----
def test_french_catalog_matches_default_string_keys(self)
⋮----
default = resource_names(VALUES)
french = resource_names(VALUES_FR)
⋮----
def test_accessibility_pane_title_resources_exist_in_both_locales(self)
⋮----
required = {"discover_headline", "your_profile"}
⋮----
def test_main_activity_accessibility_string_references_exist(self)
⋮----
source = (ROOT / "app/src/main/java/com/whoareyou/app/MainActivity.kt").read_text(encoding="utf-8")
referenced = set(re.findall(r"R[.]string[.]([A-Za-z0-9_]+)", source))
available = resource_names(VALUES)
```

## File: test_summarize_visual_qa.py
```python
ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github" / "scripts" / "summarize-visual-qa.py"
⋮----
spec = importlib.util.spec_from_file_location("summarize_visual_qa", SCRIPT)
module = importlib.util.module_from_spec(spec)
⋮----
def write_case(root: Path, label: str, screen_ok: bool = True, ui_ok: bool = True)
⋮----
screen_text = (
ui_text = (
⋮----
class VisualQaSummaryTests(unittest.TestCase)
⋮----
def test_pass_summary(self)
⋮----
root = Path(tmp)
⋮----
output = root / "summary.md"
result = module.main(root, output)
⋮----
text = output.read_text(encoding="utf-8")
⋮----
def test_failure_summary(self)
⋮----
def test_ui_failure_summary(self)
⋮----
def test_no_reports_fails(self)
```

## File: test_trait_evolution_integration.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class TraitEvolutionIntegrationContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_bounded_history_is_persisted(self)
⋮----
def test_global_profile_builds_temporal_trait_summary(self)
⋮----
def test_ui_separates_new_evidence_from_real_movement(self)
⋮----
def test_legacy_scores_seed_history(self)
```

## File: test_trait_graph_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class TraitGraphContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_trait_graph_keeps_evidence_provenance(self)
⋮----
def test_profile_builds_graph_from_completed_dimensions(self)
⋮----
def test_profile_surfaces_fingerprint_with_non_clinical_copy(self)
⋮----
def test_trait_scores_are_bounded(self)
```

## File: test_trait_taxonomy_v2.py
```python
ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
⋮----
EN_FILES = [
FR_FILES = [
⋮----
def load(files)
⋮----
quizzes = []
⋮----
data = json.loads((ASSETS / name).read_text(encoding="utf-8"))
⋮----
class TraitTaxonomyV2ContractTest(unittest.TestCase)
⋮----
def test_all_catalogs_are_v2(self)
⋮----
def test_all_30_quizzes_have_explicit_valid_traits(self)
⋮----
quizzes = load(EN_FILES)
⋮----
def test_french_catalog_has_exact_id_and_taxonomy_parity(self)
⋮----
en = {q["id"]: q for q in load(EN_FILES)}
fr = {q["id"]: q for q in load(FR_FILES)}
⋮----
def test_growth_catalogs_are_loaded_by_repository(self)
⋮----
source = (ROOT / "app/src/main/java/com/whoareyou/app/QuizCatalog.kt").read_text(encoding="utf-8")
```

## File: test_trait_timeline_integration.py
```python
ROOT = Path(__file__).resolve().parents[1]
⋮----
class TraitTimelineIntegrationContractTest(unittest.TestCase)
⋮----
def setUp(self)
⋮----
def test_global_profile_carries_trait_timelines(self)
⋮----
def test_timeline_distinguishes_retake_and_new_evidence(self)
⋮----
def test_trait_period_comparison_tracks_score_and_confidence(self)
⋮----
def test_profile_renders_accessible_trait_history(self)
```

## File: test_validate_screenshot.py
```python
ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github" / "scripts" / "validate-screenshot.py"
⋮----
def png_chunk(kind: bytes, data: bytes) -> bytes
⋮----
payload = kind + data
⋮----
def write_rgba_png(path: Path, width: int, height: int, uniform: bool = False) -> None
⋮----
rows = bytearray()
rng = random.Random(1337)
⋮----
rows.append(0)  # filter: None
⋮----
r = g = b = 12
⋮----
r = (x * 17 + y * 3 + rng.randrange(0, 64)) & 0xFF
g = (x * 5 + y * 13 + rng.randrange(0, 64)) & 0xFF
b = (x * 11 + y * 7 + rng.randrange(0, 64)) & 0xFF
⋮----
png = bytearray(b"\x89PNG\r\n\x1a\n")
⋮----
def write_highly_compressed_non_uniform_png(path: Path, width: int, height: int) -> None
⋮----
palette = (
⋮----
band_height = max(1, height // len(palette))
⋮----
color = palette[min(y // band_height, len(palette) - 1)]
⋮----
class ScreenshotValidatorTests(unittest.TestCase)
⋮----
def run_validator(self, png: Path, expected: str)
⋮----
def test_accepts_non_uniform_screenshot(self)
⋮----
path = Path(tmp) / "screen.png"
⋮----
result = self.run_validator(path, "512x768")
⋮----
def test_accepts_highly_compressed_non_uniform_screenshot(self)
⋮----
result = self.run_validator(path, "320x640")
⋮----
def test_accepts_rotated_dimensions(self)
⋮----
def test_rejects_uniform_screenshot(self)
⋮----
result = self.run_validator(path, "1024x1024")
⋮----
def test_rejects_wrong_dimensions(self)
⋮----
result = self.run_validator(path, "720x1600")
```

## File: test_validate_ui_hierarchy.py
```python
ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / ".github" / "scripts" / "validate-ui-hierarchy.py"
⋮----
def write_xml(path: Path, bounds: str, clickable: bool = True) -> None
⋮----
class UiHierarchyValidatorTests(unittest.TestCase)
⋮----
def run_validator(self, xml: Path)
⋮----
def test_accepts_valid_48dp_plus_target(self)
⋮----
path = Path(tmp) / "ui.xml"
# 120px at 320dpi = 60dp.
⋮----
result = self.run_validator(path)
⋮----
def test_rejects_undersized_clickable(self)
⋮----
# 80px at 320dpi = 40dp.
⋮----
def test_rejects_outside_viewport(self)
⋮----
def test_fixture_is_well_formed_and_nested(self)
⋮----
root = ET.parse(path).getroot()
nodes = list(root.iter("node"))
⋮----
def test_ignores_other_packages(self)
```

## File: test_workmanager_r8_contract.py
```python
ROOT = Path(__file__).resolve().parents[1]
APP_GRADLE = ROOT / "app" / "build.gradle.kts"
PROGUARD = ROOT / "app" / "proguard-rules.pro"
⋮----
class WorkManagerR8ContractTest(unittest.TestCase)
⋮----
def test_release_graph_pins_workmanager_with_full_mode_safe_rules(self)
⋮----
gradle = APP_GRADLE.read_text(encoding="utf-8")
⋮----
def test_fix_does_not_disable_minification_or_keep_all_workmanager(self)
⋮----
proguard = PROGUARD.read_text(encoding="utf-8")
```

## File: validate_public_release_inputs.py
```python
#!/usr/bin/env python3
⋮----
PLACEHOLDER_VALUES = {
⋮----
SHA256_RE = re.compile(r"^(?:[0-9A-F]{2}:){31}[0-9A-F]{2}$")
EMAIL_RE = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")
⋮----
def load(path: Path) -> dict
⋮----
payload = json.load(handle)
⋮----
def validate(payload: dict, strict: bool = True) -> list[str]
⋮----
errors: list[str] = []
⋮----
required = (
⋮----
support_email = str(payload["support_email"]).strip()
⋮----
privacy_url = str(payload["privacy_policy_url"]).strip()
⋮----
domain = str(payload["challenge_domain"]).strip()
⋮----
fingerprint = str(payload["play_app_signing_sha256"]).strip()
⋮----
target_price = float(payload["remove_ads_target_price_eur"])
⋮----
telemetry_enabled = payload["production_telemetry_enabled"]
⋮----
retention = payload.get("production_telemetry_retention_days")
contact = payload.get("production_telemetry_deletion_contact")
⋮----
def main() -> int
⋮----
parser = argparse.ArgumentParser(description="Validate public Google Play release inputs")
⋮----
args = parser.parse_args()
⋮----
errors = validate(load(args.path), strict=not args.allow_placeholders)
```
