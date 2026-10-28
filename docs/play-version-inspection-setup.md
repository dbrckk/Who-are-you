# Enable read-only Google Play version inspection

## Verified blocker (28 October 2026)

The trusted main-branch workflow [Protected Play Version Inspection](../.github/workflows/play-version-inspection.yml) failed at **Verify existing Play API credential is configured** in [run 40746474147](https://github.com/dbrckk/Who-are-you/actions/runs/40746474147). The protected GitHub environment `play-internal` did not supply `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_B64`. The workflow made **no** Google Play API request and uploaded or published nothing.

A signed 0.3.0 / versionCode 5 AAB was generated in [candidate run 40701274882](https://github.com/dbrckk/Who-are-you/actions/runs/40701274882), but that proves neither that versionCode 5 is unused in the existing Play app nor that the upload certificate matches Play Console.

## One-time owner setup (works from a phone)

1. In [Google Play Console](https://play.google.com/console), open the **existing** application with package `com.whoareyou.app`. In **Setup > API access** (or the current equivalent), identify its linked Google Cloud project and an authorized service account. If a new account is needed, create it in that linked project and grant it only the minimum access needed to inspect app releases and version codes. Do not grant production-release permissions for this inspection-only task.
2. Obtain the service account **JSON key** through Google Cloud IAM / Service Accounts. Store it privately. Do not paste the JSON, private key, or encoded value in a chat, issue, commit or screenshot. A base64 string is **not encryption**.
3. Base64-encode the **entire JSON file without line breaks**, using a trusted local shell (e.g. Termux on Android): `base64 -w 0 /path/to/google-play-service-account.json`. If the shell does not support `-w`, use `base64 /path/to/google-play-service-account.json | tr -d '\n'`. Do not use a third-party online encoder.
4. Open [GitHub repository settings — environments](https://github.com/dbrckk/Who-are-you/settings/environments), choose **play-internal**, then **Environment secrets > Add secret**. Set the name exactly to `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_B64` and paste the encoded value. The secret must be on **this environment**, not only in another environment.
5. In [Actions — Protected Play Version Inspection](https://github.com/dbrckk/Who-are-you/actions/workflows/play-version-inspection.yml), open the failed run **40746474147** and choose **Re-run failed jobs**. Do not edit the request marker or change the app version merely to retry.
6. Read the resulting artifact `who-are-you-play-version-inspection-<run-id>` and inspect `versionStatus`, `highestExistingVersionCode` and `tracks`. The job does not upload an AAB, commit a Play edit, change a release track or publish.

If the inspection reports that versionCode 5 is already present, **do not upload it again**: increment the versionCode, rebuild the signed AAB with the existing upload key and repeat CI. If the API returns 403, check Play Console service-account permissions and app access rather than widening permissions indiscriminately. If Play Console's app entry or API access is unavailable, the existing Play owner must grant access.

## Separate release checks

Before any Internal-track upload, compare `play-upload-certificate.txt` in the signed candidate artifact against the **upload certificate** in Play Console, review the Health Connect `READ_STEPS` declaration and public privacy policy, and test an in-place Play-signed upgrade. A green version inspection alone does not authorize a Play upload or production release.
