# Glyph Widgets

Android home screen widgets inspired by Nothing OS: live clock and date, current weather, daily steps, battery state, and screen time. The widgets use Android `RemoteViews` and Java; weather comes from Open-Meteo and needs an internet connection. No Nothing code, logo, or font assets are included.

## Build and install

1. Open this folder in Android Studio with JDK 17, Gradle 8.9, and Android SDK Platform 35 available, then sync the Gradle project.
2. Select a connected Android phone or emulator running Android 8.0 or newer and press **Run**.
3. Open **Glyph Widgets** and set a city. Grant activity recognition for steps and Usage access for screen time when prompted from the settings buttons.
4. Long-press the home screen, open **Widgets**, and add any of the five Glyph widgets.

The project targets Android 15 (API 35). This workspace does not include a Gradle wrapper or a prebuilt APK.

## Build the APK on GitHub

The workflow in `.github/workflows/android-apk.yml` builds a debug APK on every push and can also be started manually from the repository's **Actions** tab. After the run succeeds, open the run, find **Artifacts**, and download `glyph-widgets-debug-apk` (a ZIP containing `app-debug.apk`). The artifact is retained for 14 days.

## Data and refresh behavior

- The clock is an Android `TextClock`, so it ticks locally each minute without a network request.
- Weather is fetched for the city entered in settings using Open-Meteo's public geocoding and forecast endpoints.
- Steps use the phone's hardware step-counter sensor. The first reading establishes today's baseline; readings are refreshed when the widget updates or the app is opened.
- Battery percentage is read from Android's sticky battery status.
- Screen time sums foreground usage reported by Android after the user enables Usage access.
- Android may defer periodic widget updates to conserve battery. Tap a widget or open the app to refresh its data.
