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
assetlinks.template.json
internal-test-release.json
m28-external-inputs.template.json
m28-readiness.json
m30-play-build-contract.json
m31-readiness.json
m32-play-candidate-contract.json
m32-readiness.json
m33-readiness.json
play-internal-test-metadata.json
play-signing-contract.json
public-release-inputs.template.json
```

# Files

## File: assetlinks.template.json
```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.whoareyou.app",
      "sha256_cert_fingerprints": [
        "REPLACE_WITH_PLAY_APP_SIGNING_SHA256"
      ]
    }
  }
]
```

## File: internal-test-release.json
```json
{
  "applicationId": "com.whoareyou.app",
  "versionCode": 1,
  "versionName": "0.1.0",
  "minSdk": 26,
  "targetSdk": 36,
  "compileSdk": 37,
  "release": {
    "minifyEnabled": false,
    "artifact": "app/build/outputs/bundle/release/app-release.aab",
    "githubArtifactName": "who-are-you-release-aab"
  },
  "billing": {
    "removeAdsProductId": "remove_ads_lifetime"
  },
  "network": {
    "telemetryEndpointScheme": "https",
    "appPermissions": [
      "android.permission.INTERNET"
    ]
  },
  "admob": {
    "ciFallbackUsesGoogleTestIds": true,
    "productionIdsRequiredBeforePlayTesting": true
  },
  "appLinks": {
    "host": "dbrckk.github.io",
    "pathPrefix": "/Who-are-you/challenge/",
    "autoVerify": true,
    "productionAssetLinksRequired": true
  },
  "externalRequirementsBeforeRealInternalTest": [
    "Configure Play App Signing/upload key",
    "Create Play Billing product remove_ads_lifetime",
    "Provide production AdMob app and interstitial IDs",
    "Publish production assetlinks.json using Play App Signing SHA-256",
    "Publish privacy policy at a stable HTTPS URL and complete Play Console forms"
  ]
}
```

## File: m28-external-inputs.template.json
```json
{
  "supportEmail": "REPLACE_BEFORE_SUBMISSION",
  "privacyPolicyUrl": "https://REPLACE_BEFORE_SUBMISSION",
  "admob": {
    "appId": "REPLACE_BEFORE_SUBMISSION",
    "interstitialId": "REPLACE_BEFORE_SUBMISSION"
  },
  "playAppSigningSha256": "REPLACE_AFTER_PLAY_APP_SIGNING",
  "billing": {
    "remove_ads_lifetime": {
      "active": false,
      "priceConfiguredInPlayConsole": false
    }
  },
  "testerGroup": "REPLACE_BEFORE_INTERNAL_ROLLOUT"
}
```

## File: m28-readiness.json
```json
{
  "milestone": "M28",
  "goal": "Play Internal testing submission handoff",
  "repoReady": [
    "release_contract_locked",
    "play_metadata_manifested",
    "store_copy_en_fr",
    "internal_release_notes_en_fr",
    "privacy_policy_draft",
    "data_safety_decision_sheet",
    "store_asset_capture_plan",
    "internal_smoke_test_matrix",
    "ci_release_aab"
  ],
  "externalBlockingInputs": [
    "play_console_app_and_app_signing",
    "upload_key",
    "production_admob_ids",
    "active_remove_ads_lifetime_product",
    "support_email",
    "published_privacy_policy_https_url",
    "completed_data_safety_and_policy_declarations",
    "play_signing_sha256_assetlinks",
    "final_store_graphics_and_real_app_screenshots",
    "tester_list_or_group"
  ],
  "recommendedTelemetryForFirstInternalCandidate": "disabled_until_production_endpoint_is_documented"
}
```

## File: m30-play-build-contract.json
```json
{
  "milestone": "M30",
  "applicationId": "com.whoareyou.app",
  "versionCode": 1,
  "versionName": "0.1.0",
  "ciBuildType": "release",
  "playBuildType": "playRelease",
  "playBundleTask": ":app:bundlePlayRelease",
  "playGuardProbeTask": ":app:playReleaseGuardProbe",
  "adMob": {
    "requiredForPlayRelease": true,
    "appIdProperty": "WHO_ARE_YOU_ADMOB_APP_ID",
    "interstitialIdProperty": "WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID",
    "googleTestAppIdRejected": "ca-app-pub-3940256099942544~3347511713",
    "googleTestInterstitialIdRejected": "ca-app-pub-3940256099942544/1033173712"
  },
  "telemetry": {
    "property": "WHO_ARE_YOU_TELEMETRY_ENDPOINT",
    "required": false,
    "whenConfigured": "https_only"
  },
  "semantics": {
    "release": "CI/development release artifact; may use Google's test AdMob fallback IDs",
    "playRelease": "Google Play candidate; production-shaped non-test AdMob IDs are mandatory"
  }
}
```

## File: m31-readiness.json
```json
{
  "milestone": "M31",
  "goal": "Signed Google Play candidate without repository-stored credentials",
  "repoReadyWhen": [
    "playRelease_requires_production_admob",
    "playRelease_requires_upload_signing_inputs",
    "playRelease_bundle_signature_verifies",
    "keystore_patterns_ignored_by_git",
    "ci_exercises_ephemeral_signing_only"
  ],
  "externalStillRequired": [
    "real_upload_keystore",
    "real_upload_keystore_password",
    "real_upload_key_alias",
    "real_upload_key_password",
    "production_admob_app_id",
    "production_admob_interstitial_id"
  ]
}
```

## File: m32-play-candidate-contract.json
```json
{
  "workflow": ".github/workflows/play-candidate.yml",
  "trigger": "workflow_dispatch",
  "environment": "play-internal",
  "permissions": {
    "contents": "read"
  },
  "buildTask": ":app:bundlePlayRelease",
  "requiredSecrets": [
    "WHO_ARE_YOU_ADMOB_APP_ID",
    "WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID",
    "WHO_ARE_YOU_UPLOAD_KEYSTORE_B64",
    "WHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD",
    "WHO_ARE_YOU_UPLOAD_KEY_ALIAS"
  ],
  "optionalSecrets": [
    "WHO_ARE_YOU_TELEMETRY_ENDPOINT",
    "WHO_ARE_YOU_UPLOAD_KEY_PASSWORD"
  ],
  "keystoreLocation": "RUNNER_TEMP",
  "signatureVerification": "jarsigner -verify -verbose -certs",
  "checksum": "sha256",
  "playUploadAutomated": false,
  "artifactNameTemplate": "who-are-you-play-candidate-{versionName}-{versionCode}",
  "artifactFileTemplates": [
    "who-are-you-play-{versionName}-{versionCode}.aab",
    "play-candidate.sha256"
  ],
  "uploadKeyPasswordFallback": "WHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD"
}
```

## File: m32-readiness.json
```json
{
  "milestone": "M32",
  "goal": "Secure GitHub Actions Play candidate generation",
  "repoReady": [
    "manual_play_candidate_workflow",
    "environment_scoped_secret_contract",
    "temporary_keystore_materialization",
    "playRelease_build_path",
    "signature_verification",
    "sha256_output",
    "candidate_artifact_upload",
    "keystore_cleanup"
  ],
  "externalRequiredBeforeFirstRealRun": [
    "create_play-internal_github_environment",
    "add_real_admob_ids_as_secrets",
    "add_base64_upload_keystore_secret",
    "add_upload_keystore_password_secret",
    "add_upload_key_alias_secret",
    "add_upload_key_password_secret"
  ],
  "playPublishing": "manual_after_candidate_generation"
}
```

## File: m33-readiness.json
```json
{
  "milestone": "M33",
  "workflow": ".github/workflows/play-internal-publish.yml",
  "publisher": "tools/play_publisher.py",
  "applicationId": "com.whoareyou.app",
  "track": "internal",
  "versionCode": 1,
  "versionName": "0.1.0",
  "publishDefault": false,
  "releaseStatuses": ["draft", "completed"],
  "environment": "play-internal",
  "serviceAccountSecret": "GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_B64",
  "editFlow": [
    "insert",
    "bundle_upload",
    "track_update",
    "validate",
    "commit"
  ],
  "externalBlockers": [
    "play_console_app_exists",
    "play_app_signing_configured",
    "service_account_created",
    "service_account_play_permissions_granted",
    "google_play_service_account_secret_configured",
    "production_admob_secrets_configured",
    "upload_key_secrets_configured"
  ]
}
```

## File: play-internal-test-metadata.json
```json
{
  "track": "internal",
  "applicationId": "com.whoareyou.app",
  "versionCode": 1,
  "versionName": "0.1.0",
  "containsAds": true,
  "accountRequired": false,
  "billingProducts": [
    {
      "id": "remove_ads_lifetime",
      "type": "one_time"
    }
  ],
  "listing": {
    "defaultLanguage": "en-US",
    "additionalLanguages": ["fr-FR"],
    "englishSource": "docs/store-listing-en.md",
    "frenchSource": "docs/store-listing-fr.md",
    "releaseNotesSource": "docs/release-notes-0.1.0.md"
  },
  "privacy": {
    "policySource": "docs/privacy-policy.md",
    "dataSafetyWorksheet": "docs/play-data-safety.md",
    "publicHttpsUrlRequired": true,
    "supportContactRequired": true
  },
  "assetsRequired": [
    "app_icon",
    "phone_screenshots",
    "feature_graphic"
  ],
  "productionServicesRequired": [
    "play_app_signing_and_upload_key",
    "admob_app_id",
    "admob_interstitial_id",
    "billing_remove_ads_lifetime",
    "privacy_policy_https_url"
  ],
  "postPlaySigning": [
    "publish_assetlinks_with_play_signing_sha256",
    "verify_https_app_links_on_play_installed_build"
  ]
}
```

## File: play-signing-contract.json
```json
{
  "buildType": "playRelease",
  "bundleTask": ":app:bundlePlayRelease",
  "artifact": "app/build/outputs/bundle/playRelease/app-play-release.aab",
  "requiredSigningProperties": [
    "WHO_ARE_YOU_UPLOAD_KEYSTORE_PATH",
    "WHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD",
    "WHO_ARE_YOU_UPLOAD_KEY_ALIAS",
    "WHO_ARE_YOU_UPLOAD_KEY_PASSWORD"
  ],
  "productionAdMobRequired": true,
  "telemetryOptionalHttpsOnly": true,
  "signatureVerification": "jarsigner -verify -verbose -certs",
  "keystoreMayBeCommitted": false
}
```

## File: public-release-inputs.template.json
```json
{
  "developer_display_name": "REQUIRED",
  "support_email": "REQUIRED",
  "target_audience": "REQUIRED",
  "privacy_policy_url": "https://dbrckk.github.io/Who-are-you/privacy/",
  "challenge_domain": "dbrckk.github.io",
  "play_app_signing_sha256": "REQUIRED_AFTER_PLAY_APP_SIGNING",
  "remove_ads_product_id": "remove_ads_lifetime",
  "remove_ads_target_price_eur": 1.99,
  "production_telemetry_enabled": false,
  "production_telemetry_retention_days": null,
  "production_telemetry_deletion_contact": null
}
```
