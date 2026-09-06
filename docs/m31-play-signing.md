# M31 — Play upload signing

`playRelease` is the only candidate intended for Google Play and must be signed with an upload key supplied outside the repository.

## Required Gradle properties

The strict Play build requires all four signing values:

- `WHO_ARE_YOU_UPLOAD_KEYSTORE_PATH`
- `WHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD`
- `WHO_ARE_YOU_UPLOAD_KEY_ALIAS`
- `WHO_ARE_YOU_UPLOAD_KEY_PASSWORD`

The keystore file and passwords must never be committed.

## Production build

After M29 has generated the production AdMob properties, combine those values with the signing values and run:

```text
gradle :app:bundlePlayRelease \
  -PWHO_ARE_YOU_ADMOB_APP_ID=... \
  -PWHO_ARE_YOU_ADMOB_INTERSTITIAL_ID=... \
  -PWHO_ARE_YOU_UPLOAD_KEYSTORE_PATH=/secure/path/upload.jks \
  -PWHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD=... \
  -PWHO_ARE_YOU_UPLOAD_KEY_ALIAS=... \
  -PWHO_ARE_YOU_UPLOAD_KEY_PASSWORD=...
```

Expected bundle:

`app/build/outputs/bundle/playRelease/app-play-release.aab`

## Verification

Before upload, verify the produced AAB with JDK `jarsigner`:

```text
jarsigner -verify -verbose -certs app/build/outputs/bundle/playRelease/app-play-release.aab
```

A failing verification is a stop condition.

## GitHub Actions / CI

CI never needs the real upload key. It generates an ephemeral keystore only to prove that:

1. `playRelease` fails without signing inputs;
2. a production-shaped AdMob configuration plus complete signing inputs builds successfully;
3. the resulting AAB has a valid JAR signature.

For a future real release workflow, store the keystore as an encrypted/encoded secret or other protected external artifact and passwords as repository/environment secrets. Decode/materialize the keystore only inside the runner, build, verify, upload the artifact, then let the runner be destroyed.

## Stop conditions

Do not upload a candidate when:

- any signing input is absent;
- the keystore path does not exist;
- the wrong alias/password is supplied;
- AdMob production validation fails;
- `jarsigner -verify` fails;
- the candidate is the ordinary `release` artifact instead of `playRelease`.
