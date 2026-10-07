# 22BET Android
This is an Android WebView source project, not a compiled APK.
HTML is bundled in app/src/main/assets/index.html. No Chrome activity or URL bar is used.
Internet is required for Firebase, external libraries and remote images.
The first-run 8-ball loader uses the supplied splash.jpg. Later launches use the existing login state.
Copy and Share use native Android actions. Device Back uses the HTML navigation history; on Home, press twice within two seconds to exit.

## Build with a phone
1. Create a NEW GitHub repository, for example 22bet-android.
2. Extract this ZIP. Upload the CONTENTS of 22BET-Android to the repository ROOT (not inside a 22bet subfolder).
3. GitHub upload skips hidden folders on some phones. If .github is missing, use Add file / Create new file, filename .github/workflows/build-apk.yml, then paste the included workflow.
4. Open Actions / Build Android APK / Run workflow.
5. After it succeeds, open the run and download the 22BET-APK artifact. Extract it; install app-debug.apk.
This debug APK is for phone testing. A signed release APK/AAB and a permanent signing key are required for distribution and durable updates. Keep that key private.

## Android Studio
Open this folder with Android Studio and sync. Build > Build APK(s).
No SDK was available in the generation environment; compilation and actual phone behavior have not been tested.

## Updates
Replace app/src/main/assets/index.html, increment versionCode, then rebuild.
Editing GitHub Pages does not change this bundled HTML.
Do not publish saved credentials or user database dumps in the source repository.
