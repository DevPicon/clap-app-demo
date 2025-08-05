# Clap App

## Description

Clap App is a simple Android application that demonstrates sound playback in response to user interaction. When the user taps a central button on the screen, a clap sound is played. This project showcases basic Android development concepts including UI creation with Jetpack Compose and sound management using the `MediaPlayer` API.

## Features

*   **Tap to Play**: Plays a clap sound when the main screen button is tapped.
*   **Sound Restart**: If the sound is already playing and the button is tapped again, the sound restarts from the beginning.
*   **Jetpack Compose UI**: The user interface is built entirely with Jetpack Compose, showcasing modern Android UI development practices.

## Technologies Used

*   **Kotlin**: The primary programming language for development.
*   **Jetpack Compose**: Used for building the user interface.
*   **Android MediaPlayer API**: Used for sound playback.
*   **Android SDK**: Built targeting Android 15 (API Level 35).
*   **Gradle**: For build automation.

## How to Build

1.  Clone the repository or open the project in Android Studio.
2.  Ensure you have the Android SDK for API Level 35 installed.
3.  Sync the project with Gradle files.
4.  Build and run the application on an Android device or emulator.

## Changelog

### Recent Updates

*   **(2025-08-05)** - Fix: Ensured that the clap sound restarts correctly from the beginning on every tap. The `SoundPlayer` logic was updated to handle `MediaPlayer` state more robustly.
*   **(2025-08-05)** - Chore: Updated target and compile SDK versions to 35 (Android 15) to meet Play Console requirements.

