# FatLoss

FatLoss is a lightweight native Android fitness tracker built with Kotlin, Jetpack Compose, Room, and Material 3. It is focused on daily fat-loss habits: workouts, walking, food logging, sleep, weight progress, steps, reminders, and simple analytics.

## Features

- Daily dashboard for walk, workout, diet, and sleep targets.
- Editable weekly workout plan with custom exercises.
- Flexify-inspired workout progress counts, estimated session time, and rest timer controls.
- Food log with calories and protein tracking.
- Weight, photo, sleep, and step progress tracking.
- Health Connect and device step counter support where available.
- Reminder and alarm tools for workouts, meals, and habit prompts.
- JSON/CSV backup export, import, and shareable backup text.
- Small release APK using R8 minification, resource shrinking, and lean packaging rules.

## APK

The latest built APK is included at:

```text
dist/FatLoss-v3.3.apk
```

Release package:

```text
dist/FatLoss-v3.3-release.zip
```

## Build Locally

Requirements:

- Android Studio or Android SDK
- JDK 17
- Gradle wrapper from this repository

Build release APK:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleRelease
```

Or use the helper script:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-apk.ps1
```

The release APK will be copied to `dist/FatLoss-v3.3.apk`.

## Project Structure

```text
app/src/main/java/com/sagor/fatloss/
  data/            Room entities, DAO, repository, export/import helpers
  ui/              Compose theme, navigation, reusable components
  ui/screens/      Dashboard, workout, food, progress, settings, and analytics screens
  health/          Health Connect integration
  sensors/         Step counter support
  notifications/   Reminder scheduling
  wellbeing/       Digital wellbeing integration
dist/              Release APK and release package
design/            Design references and screen mockups
```

## Version

- Application ID: `com.sagor.fatloss`
- Version name: `3.3`
- Version code: `6`
- Minimum SDK: `26`
- Target SDK: `34`

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
