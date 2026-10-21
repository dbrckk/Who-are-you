#!/usr/bin/env bash
# Prove the existing Play upload private key can sign, without exposing secrets.
set -euo pipefail
umask 077

: "${RUNNER_TEMP:?RUNNER_TEMP is required}"
: "${GITHUB_OUTPUT:?GITHUB_OUTPUT is required}"
: "${UPLOAD_KEYSTORE_PATH:?UPLOAD_KEYSTORE_PATH is required}"
: "${UPLOAD_KEYSTORE_PASSWORD:?UPLOAD_KEYSTORE_PASSWORD is required}"
: "${UPLOAD_KEY_ALIAS:?UPLOAD_KEY_ALIAS is required}"
test -s "$UPLOAD_KEYSTORE_PATH"

# Keytool -certreq is not a valid key-password proof for PKCS12 keystores:
# it can accept an incorrect -keypass and still return success. Jarsigner
# exercises the same Java private-key decryption used by Android bundle signing.
probe_dir="$(mktemp -d "$RUNNER_TEMP/play-upload-key-probe-XXXXXXXX")"
trap 'rm -rf "$probe_dir"' EXIT
printf 'Temporary Play upload key signing probe\n' > "$probe_dir/probe.txt"
(cd "$probe_dir" && jar --create --file probe.jar probe.txt >/dev/null 2>&1)

can_sign_with() {
  rm -f "$probe_dir/signed.jar"
  # Passwords are passed by environment names, never as CLI arguments or logs.
  if ! PLAY_UPLOAD_KEY_PASSWORD_CANDIDATE="$1" jarsigner \
    -keystore "$UPLOAD_KEYSTORE_PATH" \
    -storepass:env UPLOAD_KEYSTORE_PASSWORD \
    -keypass:env PLAY_UPLOAD_KEY_PASSWORD_CANDIDATE \
    -signedjar "$probe_dir/signed.jar" \
    "$probe_dir/probe.jar" "$UPLOAD_KEY_ALIAS" </dev/null >/dev/null 2>&1; then
    return 1
  fi
  test -s "$probe_dir/signed.jar"
}

if [[ -n "${UPLOAD_KEY_PASSWORD:-}" ]] && can_sign_with "$UPLOAD_KEY_PASSWORD"; then
  source="explicit"
elif can_sign_with "$UPLOAD_KEYSTORE_PASSWORD"; then
  source="store"
  if [[ -n "${UPLOAD_KEY_PASSWORD:-}" ]]; then
    echo "::warning::The configured upload key password cannot sign; the existing keystore password works and will be used."
  fi
else
  echo "::error::The existing Play upload private key cannot sign with either configured password. Verify the protected GitHub secrets; do not create a replacement key."
  exit 1
fi

printf 'source=%s\n' "$source" >> "$GITHUB_OUTPUT"
echo "Verified existing Play upload private key by signing a disposable JAR (password source: $source)."
