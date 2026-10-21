#!/usr/bin/env bash
# Verify an existing Play upload private key without exporting it or logging passwords.
set -euo pipefail
umask 077

: "${RUNNER_TEMP:?RUNNER_TEMP is required}"
: "${GITHUB_OUTPUT:?GITHUB_OUTPUT is required}"
: "${UPLOAD_KEYSTORE_PATH:?UPLOAD_KEYSTORE_PATH is required}"
: "${UPLOAD_KEYSTORE_PASSWORD:?UPLOAD_KEYSTORE_PASSWORD is required}"
: "${UPLOAD_KEY_ALIAS:?UPLOAD_KEY_ALIAS is required}"
test -s "$UPLOAD_KEYSTORE_PATH"

csr_file="$(mktemp "$RUNNER_TEMP/play-upload-key-check-XXXXXXXX.csr")"
trap 'rm -f "$csr_file"' EXIT

can_unlock_key() {
  rm -f "$csr_file"
  keytool -certreq \
    -alias "$UPLOAD_KEY_ALIAS" \
    -keystore "$UPLOAD_KEYSTORE_PATH" \
    -storepass "$UPLOAD_KEYSTORE_PASSWORD" \
    -keypass "$1" \
    -file "$csr_file" </dev/null >/dev/null 2>&1
}

if [[ -n "${UPLOAD_KEY_PASSWORD:-}" ]] && can_unlock_key "$UPLOAD_KEY_PASSWORD"; then
  source="explicit"
elif can_unlock_key "$UPLOAD_KEYSTORE_PASSWORD"; then
  source="store"
  if [[ -n "${UPLOAD_KEY_PASSWORD:-}" ]]; then
    echo "::warning::The configured Play upload key password cannot unlock the private key; the existing keystore password works and will be used."
  fi
else
  echo "::error::The existing Play upload private key could not be unlocked with either configured password. Verify the protected GitHub secrets and original upload keystore; do not generate a replacement key."
  exit 1
fi

test -s "$csr_file"
printf 'source=%s\n' "$source" >> "$GITHUB_OUTPUT"
echo "Verified existing Play upload private key (password source: $source)."
