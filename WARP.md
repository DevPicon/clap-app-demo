# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a **Kotlin Multiplatform (KMP)** demo application that plays a clap sound when the screen is touched. The project demonstrates cross-platform code sharing between Android and iOS using the expect/actual pattern, with ~80% of business logic shared across platforms.

**Key Technologies:**
- Kotlin Multiplatform 1.9.23
- Jetpack Compose (Android UI)
- SwiftUI (iOS UI)
- Coroutines for async operations
- MediaPlayer (Android) / AVAudioPlayer (iOS)

## Architecture

### Module Structure
```
├── app/           # Android application module
├── shared/        # Kotlin Multiplatform shared code
│   ├── commonMain/    # Platform-agnostic business logic
│   ├── androidMain/   # Android-specific implementations
│   └── iosMain/       # iOS-specific implementations
└── iosApp/        # iOS application (Xcode project)
```

### Key Components

**Shared Business Logic (`shared/src/commonMain/`):**
- `SoundPlayer.kt`: Interface defining audio playback contract
- `ClapViewModel.kt`: Shared ViewModel using expect/actual pattern
- State management with `StateFlow` for reactive UI updates

**Platform Implementations:**
- `PlatformSoundPlayer.kt`: expect/actual implementations for audio playback
- Android: Uses MediaPlayer with resource-based audio files
- iOS: Uses AVAudioPlayer with bundle-based audio files

**MVVM Pattern:**
- `ClapViewModel` manages clap count and audio playback state
- Platform-specific ViewModels handle initialization differences
- StateFlow provides reactive state updates to UI layers

## Common Development Commands

### Build Commands
```bash
# Build everything (both platforms)
./gradlew build

# Build Android app only
./gradlew :app:assembleDebug

# Build shared module only
./gradlew :shared:build

# Build iOS framework
./gradlew :shared:linkReleaseFrameworkIosArm64
./gradlew :shared:linkReleaseFrameworkIosX64
```

### Development Workflow
```bash
# Use the build script for convenience
./build.sh all        # Build everything
./build.sh android    # Android only
./build.sh ios        # iOS framework only
./build.sh shared     # Shared module only
./build.sh clean      # Clean builds
./build.sh test       # Run tests

# Install Android debug build
./gradlew :app:installDebug

# Run Android tests
./gradlew :app:testDebugUnitTest

# Run shared module tests
./gradlew :shared:testDebugUnitTest
```

### Code Quality
```bash
# Run Detekt static analysis
./gradlew detekt

# Auto-correct Detekt issues
./gradlew detektAutoCorrect
```

## Development Patterns

### Adding Shared Functionality
1. Define interface/expect class in `shared/src/commonMain/`
2. Implement actual class in `shared/src/androidMain/` and `shared/src/iosMain/`
3. Update both Android and iOS UI layers to use the shared logic

### expect/actual Pattern Example
```kotlin
// commonMain: Define contract
expect class PlatformSoundPlayer : SoundPlayer

// androidMain: Android implementation
actual class PlatformSoundPlayer : SoundPlayer {
    // Android-specific MediaPlayer code
}

// iosMain: iOS implementation  
actual class PlatformSoundPlayer : SoundPlayer {
    // iOS-specific AVAudioPlayer code
}
```

### StateFlow Usage
- All shared state uses `StateFlow` for cross-platform reactivity
- ViewModels expose read-only StateFlow properties
- UI layers collect/observe StateFlow in platform-specific ways

## Important Files & Locations

### Build Configuration
- `build.gradle.kts` (root): KMP plugins and Detekt configuration
- `shared/build.gradle.kts`: Multiplatform targets and dependencies
- `gradle/libs.versions.toml`: Centralized dependency versions
- `gradle.properties`: KMP compatibility settings

### Audio Resources
- **Android**: Place audio files in `app/src/main/res/raw/`
- **iOS**: Add audio files to iOS app bundle via Xcode

### Platform-Specific Entry Points
- **Android**: `app/src/main/java/.../MainActivity.kt`
- **iOS**: `iosApp/iosApp/ContentView.swift`

## Development Setup Requirements

### Prerequisites
- Android Studio (latest)
- Xcode 15.0+ (for iOS development)
- Kotlin 1.9.23+
- Gradle 8.0+
- JVM 17 (configured in all modules)

### Configuration Notes
- All modules use Java 17 compatibility
- KMP warnings are suppressed via gradle.properties
- Detekt is configured with auto-correction enabled
- iOS targets: iosX64, iosArm64, iosSimulatorArm64

## Testing Strategy

### Shared Logic Testing
- Focus tests on business logic in `commonMain`
- Test ViewModel state changes and audio playback coordination
- Mock platform-specific implementations for unit testing

### Platform-Specific Testing
- Test actual implementations separately on each platform
- Verify MediaPlayer integration on Android
- Test AVAudioPlayer integration on iOS (requires iOS simulator/device)

## Known Patterns & Conventions

### State Management
- Use `MutableStateFlow` internally, expose as `StateFlow`
- Initialize state in ViewModel constructors
- Handle platform-specific initialization in actual implementations

### Resource Management
- Always call `release()` on ViewModels during cleanup
- Handle MediaPlayer lifecycle properly on Android
- Implement proper resource cleanup for both platforms

### Error Handling
- Log platform-specific errors in actual implementations
- Provide fallback behavior for audio playback failures
- Use try-catch blocks around platform-specific audio operations