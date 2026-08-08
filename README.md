<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/566f6781-55e2-44c1-b5c5-40487c89dcd6

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio) (or a JDK 17+ for command-line builds)

1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. (Optional) Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example). Not required for a plain debug build.
5. Run the app on an emulator or physical device

## Build from the command line / CI

This project ships with the Gradle wrapper, so no local Gradle install is required:

```bash
./gradlew assembleDebug      # builds an unsigned-but-debug-keystore-signed APK
```

Debug builds automatically use the standard Android debug keystore — nothing to configure.

To produce a signed **release** build (e.g. for Google Play), set these environment
variables before building (for example as GitHub Actions repository secrets):

- `KEYSTORE_PATH` – path to your upload keystore (`.jks`)
- `STORE_PASSWORD`
- `KEY_ALIAS` (defaults to `upload`)
- `KEY_PASSWORD`

```bash
KEYSTORE_PATH=/path/to/upload-key.jks STORE_PASSWORD=... KEY_PASSWORD=... ./gradlew assembleRelease
```

If these variables aren't set, `assembleRelease` still succeeds but produces an
**unsigned** APK. A GitHub Actions workflow (`.github/workflows/android-build.yml`)
is included and builds `assembleDebug` on every push/PR to verify the project compiles.

If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.
