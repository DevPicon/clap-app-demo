# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a **Kotlin Multiplatform (KMP)** demo application that plays a clap sound when the screen is touched. The project demonstrates cross-platform code sharing between Android and iOS using Koin dependency injection and the expect/actual pattern, with ~80% of business logic shared across platforms.

**Key Technologies:**
- Kotlin Multiplatform 1.9.23
- Koin dependency injection framework (cross-platform)
- Jetpack Compose (Android UI)
- SwiftUI (iOS UI)
- Coroutines for async operations
- MediaPlayer (Android) / AVAudioPlayer (iOS)

## Architecture

### Module Structure
```
├── app/           # Android application module
├── shared/        # Kotlin Multiplatform shared code
│   ├── commonMain/    # Platform-agnostic business logic + DI setup
│   ├── androidMain/   # Android-specific implementations + Koin module
│   └── iosMain/       # iOS-specific implementations + Koin module
└── iosApp/        # iOS application (Xcode project)
```

### Dependency Injection with Koin

The project uses **Koin** for cross-platform dependency injection. This approach replaces traditional expect/actual classes with:
- **Function-based DI**: `expect fun getPlatformModule(): Module` in commonMain
- **Platform modules**: AndroidModule and IosModule implement the expected function
- **Service locator pattern**: ViewModels and services are retrieved via Koin's container

**Why Koin?**
- Single dependency resolution for both platforms
- Reduced expect/actual boilerplate for complex dependencies
- Better control over platform-specific initialization (Android Context, iOS frameworks)
- Cleaner ViewModel/service instantiation in UI layers

**DI Module Structure:**
- `shared/src/commonMain/KoinSetup.kt`: Defines `expect fun getPlatformModule()`
- `shared/src/androidMain/AndroidModule.kt`: Provides Android-specific implementations
- `shared/src/iosMain/IosModule.kt`: Provides iOS-specific implementations

### Key Components

**Shared Business Logic (`shared/src/commonMain/`):**
- `SoundPlayer.kt`: Interface defining audio playback contract with `expect fun buildPlatformSoundPlayer()`
- `ClapViewModel.kt`: Shared ViewModel (no expect/actual needed - injected via Koin)
- `KoinSetup.kt`: Defines `expect fun getPlatformModule()` for DI resolution
- State management with `StateFlow` for reactive UI updates

**Platform Implementations:**
- `shared/src/androidMain/PlatformSoundPlayer.kt`: Android implementation using MediaPlayer
- `shared/src/androidMain/AndroidModule.kt`: Koin module providing Android dependencies (ResourceReader, AndroidSoundPlayer, ClapViewModel)
- `shared/src/androidMain/ResourceReader.kt`: Utility for loading raw audio resources via Android Context
- `shared/src/iosMain/PlatformSoundPlayer.kt`: iOS implementation (stub - AVAudioPlayer pending)
- `shared/src/iosMain/IosModule.kt`: Koin module providing iOS dependencies (NativeSoundPlayer, ClapViewModel)
- `shared/src/iosMain/KoinHelper.kt`: Helper class for iOS to initialize and access Koin

**MVVM Pattern:**
- `ClapViewModel` receives SoundPlayer via constructor injection (Koin-managed)
- Manages clap count and audio playback state via `StateFlow`
- StateFlow provides reactive state updates to UI layers (Android Compose and iOS SwiftUI)

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
1. Define interface in `shared/src/commonMain/` (e.g., `SoundPlayer.kt`)
2. Create implementations in `shared/src/androidMain/` and `shared/src/iosMain/`
3. Register implementations in `AndroidModule.kt` and `IosModule.kt`
4. UI layers retrieve dependencies via Koin (no manual wiring needed)

### Koin-Based Dependency Injection Pattern

**Example: SoundPlayer and ClapViewModel**

```kotlin
// commonMain: Define interface and expect function
// SoundPlayer.kt
interface SoundPlayer {
    fun playClapSound()
    fun release()
    val isPlaying: StateFlow<Boolean>
}

expect fun buildPlatformSoundPlayer(): SoundPlayer

// KoinSetup.kt
expect fun getPlatformModule(): Module

// ClapViewModel.kt - receives dependencies via constructor (Koin will inject)
class ClapViewModel(private val soundPlayer: SoundPlayer) {
    fun onClapClick() {
        soundPlayer.playClapSound()
    }
}

// androidMain: Provide Android implementations via Koin
// AndroidModule.kt
val androidModule = module {
    single { ResourceReader(androidContext()) }
    single<SoundPlayer> { AndroidSoundPlayer(get()) }
    single { ClapViewModel(get()) }  // get() resolves SoundPlayer from container
}

actual fun getPlatformModule() = androidModule

// iosMain: Provide iOS implementations via Koin
// IosModule.kt
val iosModule = module {
    single<SoundPlayer> { NativeSoundPlayer() }
    single { ClapViewModel(get()) }  // get() resolves SoundPlayer from container
}

actual fun getPlatformModule() = iosModule

// Get instances from Koin using KoinComponent
actual fun buildPlatformSoundPlayer(): SoundPlayer = object : KoinComponent {}.get()
```

**Android UI Integration:**
```kotlin
// MainActivity.kt - ClapApplication initializes Koin, MainActivity retrieves ViewModel
val viewModel: ClapViewModel = koinViewModel()  // Jetpack integration via Koin
```

**iOS UI Integration:**
```swift
// ContentView.swift
init() {
    self.viewModel = KoinHelper().getClapViewModel()  // Uses Koin to get instance
}
```

### StateFlow Usage
- All shared state uses `StateFlow` for cross-platform reactivity
- ViewModels expose read-only StateFlow properties
- Android: Use `.collectAsState()` in Compose to observe changes
- iOS: Manual observation or KMP interop to collect StateFlow

## Koin Initialization Requirements

**CRITICAL:** Koin must be initialized in both platforms before the app can run.

### Android Initialization
The `ClapApplication` class must be declared in `AndroidManifest.xml`:

```kotlin
// app/src/main/java/pe/devpicon/android/clapapp/ClapApplication.kt
class ClapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ClapApplication)
            modules(getPlatformModule())  // Loads AndroidModule from shared
        }
    }
}
```

**AndroidManifest.xml must reference it:**
```xml
<application android:name=".ClapApplication" ... >
```

### iOS Initialization
Call `KoinHelper.doInitKoin()` before accessing any ViewModels:

```swift
// iosApp/iosApp/App.swift (or AppDelegate)
import shared

@main
struct MyApp: App {
    init() {
        KoinHelper.companion.doInitKoin()  // Initialize Koin with IosModule
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## Important Files & Locations

### Dependency Injection Files
- `shared/src/commonMain/kotlin/.../KoinSetup.kt`: Defines `expect fun getPlatformModule()`
- `shared/src/androidMain/kotlin/.../AndroidModule.kt`: Android Koin module with ResourceReader, AndroidSoundPlayer, ClapViewModel
- `shared/src/androidMain/kotlin/.../ResourceReader.kt`: Utility for accessing Android raw resources
- `shared/src/iosMain/kotlin/.../IosModule.kt`: iOS Koin module with NativeSoundPlayer, ClapViewModel
- `shared/src/iosMain/kotlin/.../KoinHelper.kt`: Exposes Koin initialization and instance retrieval for Swift code
- `app/src/main/java/.../ClapApplication.kt`: Android Application class that initializes Koin

### Build Configuration
- `build.gradle.kts` (root): KMP plugins and Detekt configuration
- `shared/build.gradle.kts`: Multiplatform targets and dependencies
- `gradle/libs.versions.toml`: Centralized dependency versions
- `gradle.properties`: KMP compatibility settings

### Audio Resources
- **Android**: Place audio files in `app/src/main/res/raw/`
- **iOS**: Add audio files to iOS app bundle via Xcode

### Platform-Specific Entry Points
- **Android**: `app/src/main/java/.../MainActivity.kt` (retrieves ViewModel from Koin)
- **iOS**: `iosApp/iosApp/ContentView.swift` (retrieves ViewModel via KoinHelper)

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

## Known Limitations

### iOS Audio Implementation
- Current iOS audio is a **stub implementation** (NativeSoundPlayer) that simulates playback without actual sound
- **TODO:** Configure AVFoundation framework linkage in `shared/build.gradle.kts`
- **TODO:** Implement full AVAudioPlayer integration with proper bundle resource loading
- **Workaround:** The stub correctly manages UI state (isPlaying StateFlow) so UI updates work correctly even without audio

### Platform Requirements
- Requires **Xcode command line tools** for iOS development and builds
- iOS deployment target must be compatible with KMP and Koin versions
- Android requires API level 21+ (specified in build.gradle.kts)

### Koin-Specific Constraints
- Must initialize Koin before accessing any DI-dependent components
- Forgetting to call `ClapApplication` in Android or `KoinHelper.doInitKoin()` in iOS will cause runtime crashes
- All expect functions must be paired with exactly one actual implementation per platform