<p align="center">
  <h1 align="center">Antogram for Android</h1>
</p>

<p align="center">
  <img src="./asset/divider.gif" width="440" height="40" />
</p>

## <img src="./asset/icon.gif" width="44px" /> About

```sh
antogram@android:~$ whoami
Antogram Android Client

antogram@android:~$ echo "base"
Telegram Android source tree + custom changes

antogram@android:~$ echo "repo"
https://github.com/Anto426/Antogram
```

Antogram is an Android client built on top of the Telegram Android codebase.
This project follows Telegram API/MTProto rules and keeps compatibility with upstream architecture where possible.

<p align="center">
  <img src="./asset/divider.gif" width="440" height="40" />
</p>

## <img src="./asset/icon2.gif" width="48px" /> Highlights

- Kotlin-first migration across camera and recorder flows
- CameraX integration replacing legacy camera paths
- Multiple app modules and build flavors
- Native layer via CMake/NDK

<p align="center">
  <img src="./asset/divider.gif" width="440" height="40" />
</p>

## Toolchain

Use a setup aligned with current Gradle files:

- Android Studio: recent stable
- JDK: 21
- Android SDK Platform: 36
- Android NDK: `21.4.7075529`

<p align="center">
  <img src="./asset/divider.gif" width="440" height="40" />
</p>

## <img src="./asset/icon3.gif" width="48px" /> Quick Start

```sh
git clone https://github.com/Anto426/Antogram.git
cd Antogram
```

1. Put your keystore in `TMessagesProj/config`.
2. Set these in `gradle.properties`:
`RELEASE_KEY_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_STORE_PASSWORD`.
3. Add `google-services.json` in modules that require Firebase.
4. Open the project folder directly in Android Studio.
5. Configure runtime/build constants in:
`TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java`.

<p align="center">
  <img src="./asset/divider.gif" width="440" height="40" />
</p>

## Build

Windows:

```powershell
.\gradlew.bat assemble
```

Linux/macOS:

```bash
./gradlew assemble
```

Example focused compile:

```bash
./gradlew :TMessagesProj:compileDebugKotlin :TMessagesProj:compileDebugJavaWithJavac
```

<p align="center">
  <img src="./asset/divider.gif" width="440" height="40" />
</p>

## Project Layout

- `TMessagesProj/`: core Android library/module
- `TMessagesProj_App/`: app wrapper module
- `TMessagesProj_AppStandalone/`: standalone flavor packaging
- `TMessagesProj_AppHockeyApp/`: Hockey/AppCenter-oriented flavor packaging
- `TMessagesProj_AppTests/`: test app/module
- `Tools/`: helper scripts and tooling

<p align="center">
  <img src="./asset/divider.gif" width="440" height="40" />
</p>

## <img src="./asset/icon4.gif" width="48px" /> Compliance Notes

If you redistribute builds:

1. Create your own Telegram `api_id` and `api_hash`:
https://core.telegram.org/api/obtaining_api_id
2. Use your own app identity/branding.
3. Respect MTProto security guidance:
https://core.telegram.org/mtproto/security_guidelines
4. Publish source changes as required by upstream licenses.

<p align="center">
  <img src="./asset/divider.gif" width="440" height="40" />
</p>

## References

- Telegram API: https://core.telegram.org/api
- MTProto: https://core.telegram.org/mtproto
- Reproducible builds: https://core.telegram.org/reproducible-builds
- Android localization portal: https://translations.telegram.org/en/android/
