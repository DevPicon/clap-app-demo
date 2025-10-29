# Claude Context - Clap App Demo

## Project Overview
The Clap App Demo is a simple Android application that has been successfully migrated to Kotlin Multiplatform (KMP). The app plays a clap sound effect when the user taps the screen.

## Current Architecture (Post-KMP Migration)

### Technology Stack
- Kotlin 2.1.21
- Gradle 8.5
- Kotlin Multiplatform
- Koin 4.0.0 (Dependency Injection)
- Jetpack Compose (Android UI)
- SwiftUI (iOS UI)

### Module Structure
```
clap-app-demo/
├── app/                          # Android application module
│   ├── src/main/
│   │   ├── java/.../
│   │   │   ├── ClapApplication.kt    # Koin initialization for Android
│   │   │   └── MainActivity.kt        # Compose UI with Koin injection
│   │   └── res/raw/claps.ogg         # Android audio resource (OGG format)
│   └── build.gradle.kts              # Includes product flavors (dev/prod)
├── shared/                       # Kotlin Multiplatform shared module
│   ├── src/
│   │   ├── commonMain/           # Shared business logic
│   │   │   ├── kotlin/.../
│   │   │   │   ├── ClapViewModel.kt      # MVVM ViewModel with constructor injection
│   │   │   │   ├── SoundPlayer.kt         # expect function interface
│   │   │   │   └── KoinSetup.kt           # Common Koin configuration
│   │   │   └── resources/                 # Shared resources directory
│   │   ├── androidMain/          # Android-specific implementations
│   │   │   └── kotlin/.../
│   │   │       ├── AndroidModule.kt       # Android Koin module
│   │   │       ├── PlatformSoundPlayer.kt # actual fun for Android
│   │   │       └── ResourceReader.kt      # Resource access helper
│   │   └── iosMain/              # iOS-specific implementations
│   │       └── kotlin/.../
│   │           ├── IosModule.kt           # iOS Koin module
│   │           ├── KoinHelper.kt          # Swift-Kotlin bridge for Koin
│   │           └── PlatformSoundPlayer.kt # actual fun stub (delegates to Swift)
│   └── build.gradle.kts          # Static framework configuration
└── iosApp/                       # iOS application
    ├── iosApp/
    │   ├── iosAppApp.swift           # Koin initialization
    │   ├── ContentView.swift         # SwiftUI with KoinHelper
    │   ├── AudioPlayer.swift         # Swift audio player implementation
    │   ├── claps.wav                 # iOS audio resource (WAV format, 942KB)
    │   └── README_AUDIO.md           # Audio setup documentation
    └── iosApp.xcodeproj/
```

### Key Architectural Decisions

#### 1. Koin Dependency Injection (Cross-Platform)
- **Common Koin Setup**: `KoinSetup.kt` provides `initKoin()` function
- **Platform Modules**: Separate modules for Android and iOS platform-specific dependencies
- **Constructor Injection**: ClapViewModel uses constructor injection for SoundPlayer
- **Android**: Initialized in `ClapApplication` (Application class)
- **iOS**: Initialized in `iosAppApp.swift` via `KoinHelper.doInitKoin()`

#### 2. expect/actual Pattern (Functions, Not Classes)
- **Interface**: `expect fun createSoundPlayer(context: Any?): SoundPlayer`
- **Android Implementation**: Uses MediaPlayer with OGG resource
- **iOS Implementation**: Stub function (actual audio handled by Swift AudioPlayer)
- **Why Functions**: Simpler than expect/actual classes, better interop with Swift

#### 3. iOS Audio Strategy
- **Problem**: AVFoundation Kotlin/Native interop is complex
- **Solution**: Swift AudioPlayer wrapper class
- **Integration**: ContentView.swift uses both KoinHelper (for ViewModel) and AudioPlayer
- **Format**: WAV file (better iOS compatibility than OGG)

#### 4. Product Flavors (Android Only)
- **dev**: `pe.devpicon.android.clapapp.dev` - For parallel installation during testing
- **prod**: `pe.devpicon.android.clapapp` - Production package
- **Version**: 1.1.0-dev (versionCode 7)

### Critical Files

#### Android DI Setup
- `/app/src/main/java/pe/devpicon/android/clapapp/ClapApplication.kt`
- `/app/src/main/AndroidManifest.xml` (registered Application class)
- `/shared/src/androidMain/kotlin/pe/devpicon/clapapp/shared/AndroidModule.kt`

#### iOS DI Setup
- `/shared/src/iosMain/kotlin/pe/devpicon/clapapp/shared/KoinHelper.kt`
- `/iosApp/iosApp/iosAppApp.swift`
- `/shared/src/iosMain/kotlin/pe/devpicon/clapapp/shared/IosModule.kt`

#### Shared Business Logic
- `/shared/src/commonMain/kotlin/pe/devpicon/clapapp/shared/ClapViewModel.kt`
- `/shared/src/commonMain/kotlin/pe/devpicon/clapapp/shared/SoundPlayer.kt`
- `/shared/src/commonMain/kotlin/pe/devpicon/clapapp/shared/KoinSetup.kt`

### Build Configuration
- **Root**: `build.gradle.kts` - Project-level config
- **App**: `app/build.gradle.kts` - Android app with Koin + product flavors
- **Shared**: `shared/build.gradle.kts` - KMP module with static framework
- **Dependencies**: `gradle/libs.versions.toml` - Centralized version catalog

### CI/CD Configuration

#### GitHub Actions Workflows

**1. PR Checks** (`.github/workflows/pr-checks.yml`)
- **Trigger**: Pull requests to `master` branch
- **Purpose**: Automated code quality and validation
- **Steps**:
  - Validates shared KMP module for all targets (Android + iOS)
  - Runs all platform tests including iOS compilation
  - Executes Detekt static analysis with PR reporting
  - Runs unit tests
  - Builds debug APK
- **Key Features**:
  - Early detection of KMP compilation issues
  - Ensures iOS targets remain compilable
  - Comprehensive Detekt report merging and PR comments
  - Gradle caching for faster builds

**2. Release Workflow** (`.github/workflows/release.yml`)
- **Trigger**: Manual (workflow_dispatch)
- **Purpose**: Automated deployment to Google Play Store
- **Jobs**:
  1. **Test Job**:
     - Validates shared KMP module
     - Runs all platform tests
     - Executes Android unit tests
  2. **Distribute Job**:
     - Auto-bumps version code
     - Builds **prod** flavor release APK (`assembleProdRelease`)
     - Signs APK with release keystore
     - Deploys to Google Play (production track, draft status)
- **Important**: Uses production flavor, not dev flavor
- **Requirements**: GitHub secrets for signing and Play Store API

#### Gradle Verification Tasks
- `./gradlew :shared:check` - Validates shared module (all targets + Detekt)
- `./gradlew :shared:allTests` - Runs tests for all targets (Android, iosX64, iosArm64, iosSimulatorArm64)
- `./gradlew :app:testDebug` - Android-specific unit tests
- `./gradlew :app:assembleProdRelease` - Build production release APK
- `./gradlew detekt` - Static code analysis across all modules

#### CI/CD Best Practices
1. **Validate shared before platform**: Always check shared module before Android/iOS builds
2. **Test all targets**: Even without iOS deployment, compile and test iOS targets
3. **Fail fast**: Run quick validations before expensive operations
4. **Gradle caching**: Enabled in workflows for faster builds
5. **Product flavors**: Release workflow explicitly uses `prod` flavor

### Testing Status
- Android dev flavor: Tested on Samsung Galaxy Z Flip 5 (physical device)
- iOS: Tested on iPhone 16 Pro Simulator
- Koin DI: Working on both platforms
- Audio playback: Functional on both platforms

### Known Limitations
1. iOS audio is handled entirely in Swift (not through shared Kotlin code)
2. PlatformSoundPlayer.kt on iOS is a stub - actual implementation in AudioPlayer.swift
3. Different audio formats: OGG (Android) vs WAV (iOS)

### Migration History
- **Initial**: Android-only app with MediaPlayer
- **KMP Migration**: Shared business logic, platform-specific UI
- **Koin Integration**: Cross-platform dependency injection
- **iOS Audio**: Swift wrapper for AVFoundation

### Documentation
- `readme.md` - Project overview, architecture, and Koin setup
- `MIGRATION_GUIDE.md` - Step-by-step implementation guide
- `WARP.md` - AI assistant guidance for this project
- `iosApp/iosApp/README_AUDIO.md` - iOS audio setup instructions

### Branch Strategy
- **Current**: `kmp-migration` (active development)
- **Main**: `master`

### Next Steps / Future Enhancements
1. Consider unified audio resource in shared/resources
2. Explore kotlinx-media or other audio libraries for shared audio implementation
3. Add unit tests for shared ViewModel
4. Add UI tests for both platforms
5. Implement CI/CD pipeline for both platforms

### Common Issues & Solutions

#### Issue: Koin initialization fails on Android
- **Solution**: Ensure ClapApplication is registered in AndroidManifest.xml
- **Check**: `android:name=".ClapApplication"` in manifest

#### Issue: iOS cannot find KoinHelper
- **Solution**: Ensure shared framework is static (not dynamic)
- **Check**: `isStatic = true` in shared/build.gradle.kts

#### Issue: Audio not playing on iOS
- **Solution**: Ensure claps.wav is added to Xcode project
- **Check**: File appears in "Copy Bundle Resources" build phase

### Code Patterns

#### Android Activity (with Koin)
```kotlin
class MainActivity : ComponentActivity() {
    private val viewModel: ClapViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Compose UI
        }
    }
}
```

#### iOS SwiftUI (with KoinHelper)
```swift
struct ContentView: View {
    let viewModel: ClapViewModel
    let audioPlayer = AudioPlayer()

    init() {
        self.viewModel = KoinHelper().getClapViewModel()
    }
}
```

#### Shared ViewModel (with Constructor Injection)
```kotlin
class ClapViewModel(private val soundPlayer: SoundPlayer) : ViewModel() {
    fun onClapRequested() {
        soundPlayer.playClap()
    }
}
```

### Dependencies
- Koin: 4.0.0 (koin-android, koin-core)
- Compose BOM: 2025.01.00
- Kotlin: 2.1.21
- AGP: 8.1.4

### Session Notes

#### 2025-10-29 - KMP Migration
- Completed full KMP migration with Koin DI integration
- Both Android and iOS platforms tested successfully
- Unified ClapViewModel in commonMain
- Platform-specific audio implementations working
- Product flavors configured for Android parallel installation
- All documentation updated to reflect current architecture

#### 2025-10-29 - CI/CD Integration
- Updated GitHub Actions workflows for KMP architecture
- Added shared module validation to PR checks and release workflows
- Configured workflows to test all targets (Android + iOS compilation)
- Fixed release workflow to use production flavor (`assembleProdRelease`)
- Updated action versions from v3 to v4
- Added Gradle caching for improved build performance
- Created `docs/backlog.md` for project task tracking
- Created `LEARNINGS.gemini.md` for session knowledge capture
- Documented CI/CD configuration in Claude.md
