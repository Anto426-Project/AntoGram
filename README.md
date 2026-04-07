# Antogram for Android

Antogram is an Android client based on the Telegram codebase.

- Repository: https://github.com/Anto426/Antogram
- Platform docs: https://core.telegram.org/api

## Overview

This repository contains the Android sources for Antogram.
Since it is based on Telegram APIs and MTProto, follow Telegram platform rules and security requirements when redistributing builds.

## Requirements

Use a toolchain aligned with the current Gradle configuration:

- Android Studio (recent stable)
- JDK 17 or JDK 21
- Android SDK Platform 36
- Android NDK 21.4.7075529

## Quick Start

1. Clone the repository.

```bash
git clone https://github.com/Anto426/Antogram.git
```

2. Copy your release keystore into `TMessagesProj/config`.
3. Set `RELEASE_KEY_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_STORE_PASSWORD` in `gradle.properties`.
4. Configure Firebase and place `google-services.json` in each required app module.
5. Open the folder directly in Android Studio (open project, not legacy import).
6. Configure app variables in `TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java`.
7. Build from Android Studio or CLI.

Windows users can build from project root with:

```powershell
.\gradlew.bat assemble
```

## Branding and Compliance

1. Generate your own `api_id`: https://core.telegram.org/api/obtaining_api_id
2. Use your own branding and app identity for redistributed builds.
3. Review security guidance: https://core.telegram.org/mtproto/security_guidelines
4. Publish your source changes to comply with upstream licenses.

## References

- Telegram API: https://core.telegram.org/api
- MTProto: https://core.telegram.org/mtproto
- Reproducible builds: https://core.telegram.org/reproducible-builds

## Localization

If you keep the Telegram localization workflow, use:
https://translations.telegram.org/en/android/
