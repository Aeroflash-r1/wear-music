<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/ef08e6ad-9272-4536-82f2-347e46ca21dc

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## Building the release APK

Pulse talks to your own **pulse-server** (see [`server/README.md`](server/README.md)) — the Piped/Invidious backend was removed entirely. Before first run, open **Settings → Server URL** in the app and enter your server's Tailscale address (e.g. `http://phone-name.tailnet.ts.net:8080`).

The release build is signed with `app/pulse-release.keystore` (alias `pulse`) and requires three env vars:

```bash
KEYSTORE_PASSWORD=pulse123 KEY_ALIAS=pulse KEY_PASSWORD=pulse123 ./gradlew :app:assembleRelease
```

The resulting R8-minified APK is written to `app/build/outputs/apk/release/app-release.apk`.
