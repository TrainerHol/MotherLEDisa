# Releasing an APK via GitHub Actions

This repo includes a manual GitHub Actions workflow that builds a `release` APK and publishes it as an asset on a GitHub Release.

## Manual release workflow

- Run: GitHub → **Actions** → **Manual APK Release** → **Run workflow**
- Inputs:
  - **bump**: `patch`, `minor`, or `major` (default `patch`)
  - **prerelease**: if enabled, creates a prerelease (tag like `v1.2.3-rc.1`)
  - **draft**: if enabled, creates a draft release

The workflow will:

- bump `VERSION_CODE` and `VERSION_NAME` in `version.properties`
- commit the bump to the selected branch
- create and push a git tag `v<VERSION_NAME>`
- build the APK with Gradle (`:app:assembleRelease`)
- create a GitHub Release and attach `MotherLEDisa-v<VERSION_NAME>.apk`

## Signing (optional)

If you configure signing secrets, the workflow will produce a signed `app-release.apk`. Otherwise it will publish the unsigned `app-release-unsigned.apk`.

### Required secrets

Create these GitHub repository secrets:

- `ANDROID_KEYSTORE_BASE64`: base64-encoded `.jks` keystore contents
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

### Keystore path

The workflow decodes the keystore to `${GITHUB_WORKSPACE}/release-keystore.jks` and provides the path via the `ANDROID_KEYSTORE_PATH` environment variable used by Gradle.
