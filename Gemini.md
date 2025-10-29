# Gemini Context - Clap App Demo

## Project Summary
Android app successfully migrated to Kotlin Multiplatform (KMP) with shared business logic, Koin dependency injection, and platform-specific UI implementations (Jetpack Compose for Android, SwiftUI for iOS).

## Quick Reference

### Project Type
Kotlin Multiplatform Mobile (KMM) application

### Primary Functionality
Plays a clap sound effect when user taps the screen

### Current Version
1.1.0-dev (versionCode 7)

### Branch
`kmp-migration` (ahead of origin by 1 commit)

## Architecture Overview

### Multiplatform Structure
```
├── app/          → Android application (Jetpack Compose)
├── shared/       → Kotlin Multiplatform module (business logic)
└── iosApp/       → iOS application (SwiftUI)
```

### Dependency Injection: Koin 4.0.0
- **Framework**: Koin (cross-platform DI)
- **Common Setup**: `shared/src/commonMain/.../KoinSetup.kt`
- **Android Module**: `shared/src/androidMain/.../AndroidModule.kt`
- **iOS Module**: `shared/src/iosMain/.../IosModule.kt`
- **iOS Bridge**: `KoinHelper.kt` for Swift interoperability

### Design Pattern: MVVM
- **ViewModel**: `ClapViewModel` (in commonMain)
- **Injection**: Constructor-based dependency injection
- **Sound Interface**: `SoundPlayer` (expect/actual pattern)

### Platform-Specific Implementations

#### Android
- **Audio**: MediaPlayer with OGG resource (`app/src/main/res/raw/claps.ogg`)
- **DI Init**: `ClapApplication.kt` (Application class)
- **UI**: Jetpack Compose
- **Flavors**: dev and prod (for parallel installation)

#### iOS
- **Audio**: Swift AudioPlayer wrapper (AVFoundation)
- **Audio File**: WAV format (`iosApp/iosApp/claps.wav`, 942KB)
- **DI Init**: `iosAppApp.swift` via `KoinHelper.doInitKoin()`
- **UI**: SwiftUI
- **Framework**: Static framework (required for Koin interop)

## Technology Stack

### Build Tools
- Gradle: 8.5
- Kotlin: 2.1.21
- Android Gradle Plugin: 8.1.4
- Compose BOM: 2025.01.00

### Key Libraries
- Koin Core: 4.0.0
- Koin Android: 4.0.0
- Jetpack Compose (Android)
- SwiftUI (iOS)

### Gradle Configuration
- Version Catalog: `gradle/libs.versions.toml`
- Plugin Management: Version catalog plugins
- Multiplatform: `com.android.kotlin.multiplatform`

## Code Structure Highlights

### Shared Module (Business Logic)

**ViewModel - Constructor Injection**
```kotlin
// shared/src/commonMain/.../ClapViewModel.kt
class ClapViewModel(private val soundPlayer: SoundPlayer) : ViewModel() {
    private val _clapCount = MutableStateFlow(0)
    val clapCount: StateFlow<Int> = _clapCount

    fun onClapRequested() {
        soundPlayer.playClap()
        _clapCount.value++
    }
}
```

**Sound Interface - expect/actual Functions**
```kotlin
// commonMain
expect fun createSoundPlayer(context: Any?): SoundPlayer

// androidMain
actual fun createSoundPlayer(context: Any?): SoundPlayer {
    return PlatformSoundPlayer(context as Context)
}

// iosMain (stub - delegates to Swift)
actual fun createSoundPlayer(context: Any?): SoundPlayer {
    return PlatformSoundPlayer()
}
```

### Android Implementation

**Application Class**
```kotlin
// app/src/main/java/.../ClapApplication.kt
class ClapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@ClapApplication)
            modules(androidModule)
        }
    }
}
```

**Activity with Injection**
```kotlin
// app/src/main/java/.../MainActivity.kt
class MainActivity : ComponentActivity() {
    private val viewModel: ClapViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClapButton(viewModel)
        }
    }
}
```

### iOS Implementation

**App Entry Point**
```swift
// iosApp/iosApp/iosAppApp.swift
@main
struct iosAppApp: App {
    init() {
        KoinHelper().doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

**SwiftUI View with KoinHelper**
```swift
// iosApp/iosApp/ContentView.swift
struct ContentView: View {
    let viewModel: ClapViewModel
    let audioPlayer = AudioPlayer()

    init() {
        self.viewModel = KoinHelper().getClapViewModel()
    }

    var body: some View {
        Button("Tap to Clap") {
            viewModel.onClapRequested()
            audioPlayer.playSound()
        }
    }
}
```

**Koin Helper for Swift**
```kotlin
// shared/src/iosMain/.../KoinHelper.kt
class KoinHelper {
    fun doInitKoin() {
        initKoin()
    }

    fun getClapViewModel(): ClapViewModel {
        return KoinProvider.koin.get()
    }
}
```

## Build Configuration Details

### Product Flavors (Android)
```kotlin
flavorDimensions += "version"
productFlavors {
    create("dev") {
        dimension = "version"
        applicationIdSuffix = ".dev"
        versionNameSuffix = "-dev"
    }
    create("prod") {
        dimension = "version"
    }
}
```

### Shared Module Configuration
```kotlin
kotlin {
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true  // Required for Koin
        }
    }
}
```

## Testing Information

### Tested Configurations
- **Android**: Samsung Galaxy Z Flip 5 (physical device)
- **iOS**: iPhone 16 Pro Simulator
- **DI**: Koin working on both platforms
- **Audio**: Functional on both platforms

### Test Approach
- Manual testing via physical device and simulator
- Verified audio playback on tap
- Confirmed DI initialization and injection
- Validated parallel installation (dev/prod flavors)

## Migration Journey

### Phase 1: Initial Android App
- Single module Android app
- MediaPlayer for audio
- No shared code

### Phase 2: KMP Structure
- Created shared module
- Moved business logic to commonMain
- Platform-specific UI implementations

### Phase 3: Koin Integration (Latest)
- Added Koin DI framework
- Unified ClapViewModel in commonMain
- Platform modules for DI configuration
- Swift bridge (KoinHelper) for iOS
- Product flavors for Android testing

### Files Deleted During Migration
- `app/src/main/java/.../SoundPlayer.kt` (moved to shared)
- `shared/src/androidMain/.../ClapViewModel.kt` (unified in commonMain)
- `shared/src/iosMain/.../ClapViewModel.kt` (unified in commonMain)

### Files Created During Migration
- `app/src/main/java/.../ClapApplication.kt`
- `shared/src/commonMain/.../KoinSetup.kt`
- `shared/src/androidMain/.../AndroidModule.kt`
- `shared/src/androidMain/.../ResourceReader.kt`
- `shared/src/iosMain/.../KoinHelper.kt`
- `shared/src/iosMain/.../IosModule.kt`
- `iosApp/iosApp/AudioPlayer.swift`
- `iosApp/iosApp/claps.wav`
- `iosApp/iosApp/README_AUDIO.md`

## Key Learnings

### What Works Well
1. Koin 4.0.0 has excellent KMP support
2. expect/actual functions are simpler than expect/actual classes
3. Static framework is required for iOS Koin integration
4. Swift wrappers work well for complex iOS APIs (AVFoundation)
5. Product flavors enable parallel testing on same device

### Challenges Overcome
1. **iOS Audio**: AVFoundation interop complexity → Solved with Swift wrapper
2. **Koin on iOS**: Dynamic framework issues → Solved with static framework
3. **Swift Integration**: Accessing Koin from Swift → Solved with KoinHelper bridge
4. **Parallel Testing**: Single package ID limitation → Solved with product flavors

### Architecture Decisions
1. **Functions over Classes**: expect/actual functions for platform implementations
2. **Swift for iOS Audio**: Rather than complex Kotlin/Native interop
3. **Constructor Injection**: Cleaner than property injection for ViewModels
4. **Separate Modules**: Platform-specific Koin modules for clarity

## Important Configuration Notes

### AndroidManifest.xml
Must register Application class:
```xml
<application
    android:name=".ClapApplication"
    ...>
```

### Xcode Project
Audio file must be in "Copy Bundle Resources" build phase

### Gradle Properties
KMP properties configured in `gradle.properties`

### .gitignore Updates
Added exclusions for:
- `.kotlin/` (build artifacts)
- `*.xcuserstate` (Xcode user state)
- `*.xcuserdatad/` and `xcuserdata/` (Xcode user data)

## Documentation Files
- `readme.md` - Main project documentation
- `MIGRATION_GUIDE.md` - Step-by-step migration guide
- `WARP.md` - AI assistant configuration
- `iosApp/iosApp/README_AUDIO.md` - iOS audio setup guide
- `Claude.md` - Claude AI context (this file's companion)
- `Agents.md` - ChatGPT/Codex context

## Future Enhancements
1. Shared audio resources in `shared/src/commonMain/resources/`
2. Cross-platform audio library (kotlinx-media or similar)
3. Unit tests for ClapViewModel
4. UI tests for both platforms
5. iOS deployment pipeline (Fastlane + App Store Connect)
6. Code coverage reporting across all targets
7. Workflow status badges in README

## Session Summary

### 2025-10-29 - KMP Migration
- Completed comprehensive KMP migration
- Integrated Koin 4.0.0 for cross-platform DI
- Successfully tested on physical Android device and iOS simulator
- Added product flavors for parallel installation
- Updated all documentation

### 2025-10-29 - CI/CD Integration for KMP
- Updated GitHub Actions workflows to support KMP architecture
- Added shared module validation (`:shared:check`, `:shared:allTests`)
- Configured PR checks to test all targets (Android + iOS compilation)
- Fixed release workflow to use production flavor (`assembleProdRelease`)
- Updated action versions from v3 to v4 (checkout, setup-java)
- Added Gradle caching for improved build performance
- Created `docs/backlog.md` for project task tracking
- Created `LEARNINGS.gemini.md` for technical insights documentation
- Documented CI/CD strategy in all context files

## Quick Commands

### Build Android
```bash
./gradlew :app:assembleDevDebug
./gradlew :app:assembleProdDebug
```

### Build iOS Framework
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

### Clean Build
```bash
./gradlew clean
```

### Run Android
```bash
./gradlew :app:installDevDebug
```

## Contact & Resources
- Repository: GitHub (remote configured)
- Branch: `kmp-migration`
- Main Branch: `master`
- Last Updated: 2025-10-29
