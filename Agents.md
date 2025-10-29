# AI Agents Context - Clap App Demo
## For ChatGPT, Codex, and other AI coding assistants

## Project at a Glance
**Name**: Clap App Demo
**Type**: Kotlin Multiplatform Mobile (KMM)
**Purpose**: Simple tap-to-clap sound app
**Current State**: Fully migrated from Android-only to KMP with Koin DI
**Branch**: `kmp-migration`
**Version**: 1.1.0-dev (versionCode 7)

## Tech Stack Summary
- **Language**: Kotlin 2.1.21
- **Build Tool**: Gradle 8.5
- **DI Framework**: Koin 4.0.0
- **Android UI**: Jetpack Compose (BOM 2025.01.00)
- **iOS UI**: SwiftUI
- **Pattern**: MVVM with shared ViewModel

## Project Structure
```
clap-app-demo/
├── app/                    # Android app module
├── shared/                 # KMP shared module
└── iosApp/                 # iOS app
```

## Key Files & Their Purpose

### Dependency Injection Setup
| File | Purpose |
|------|---------|
| `app/src/main/java/.../ClapApplication.kt` | Android: Koin initialization in Application class |
| `app/src/main/AndroidManifest.xml` | Android: Registers ClapApplication |
| `shared/src/commonMain/.../KoinSetup.kt` | Common: initKoin() function for both platforms |
| `shared/src/androidMain/.../AndroidModule.kt` | Android: Koin module with platform dependencies |
| `shared/src/iosMain/.../IosModule.kt` | iOS: Koin module for iOS |
| `shared/src/iosMain/.../KoinHelper.kt` | iOS: Swift-Kotlin bridge for Koin access |
| `iosApp/iosApp/iosAppApp.swift` | iOS: Calls KoinHelper.doInitKoin() |

### Business Logic (Shared)
| File | Purpose |
|------|---------|
| `shared/src/commonMain/.../ClapViewModel.kt` | MVVM ViewModel with constructor injection |
| `shared/src/commonMain/.../SoundPlayer.kt` | expect fun createSoundPlayer interface |

### Platform Audio Implementations
| File | Purpose |
|------|---------|
| `shared/src/androidMain/.../PlatformSoundPlayer.kt` | actual fun - MediaPlayer wrapper |
| `shared/src/iosMain/.../PlatformSoundPlayer.kt` | actual fun - stub (delegates to Swift) |
| `iosApp/iosApp/AudioPlayer.swift` | iOS: AVFoundation audio player |

### Resources
| File | Purpose |
|------|---------|
| `app/src/main/res/raw/claps.ogg` | Android: Audio file (OGG format) |
| `iosApp/iosApp/claps.wav` | iOS: Audio file (WAV format, 942KB) |

### Build Configuration
| File | Purpose |
|------|---------|
| `build.gradle.kts` | Root project build config |
| `app/build.gradle.kts` | Android app with Koin + product flavors |
| `shared/build.gradle.kts` | KMP module with static framework |
| `gradle/libs.versions.toml` | Version catalog |
| `settings.gradle.kts` | Project structure |

### Documentation
| File | Purpose |
|------|---------|
| `readme.md` | Main documentation |
| `MIGRATION_GUIDE.md` | Step-by-step migration guide |
| `WARP.md` | AI assistant guidance |
| `iosApp/iosApp/README_AUDIO.md` | iOS audio setup |
| `Claude.md` | Claude AI context |
| `Gemini.md` | Gemini AI context |
| `Agents.md` | This file - for ChatGPT/Codex |

## Architecture Patterns

### 1. Dependency Injection (Koin)
**Android Initialization**:
```kotlin
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

**iOS Initialization**:
```swift
@main
struct iosAppApp: App {
    init() {
        KoinHelper().doInitKoin()
    }
}
```

**Usage in Android**:
```kotlin
class MainActivity : ComponentActivity() {
    private val viewModel: ClapViewModel by inject()
}
```

**Usage in iOS**:
```swift
struct ContentView: View {
    let viewModel: ClapViewModel

    init() {
        self.viewModel = KoinHelper().getClapViewModel()
    }
}
```

### 2. expect/actual Pattern (Functions)
**Declaration (commonMain)**:
```kotlin
expect fun createSoundPlayer(context: Any?): SoundPlayer
```

**Android Implementation**:
```kotlin
actual fun createSoundPlayer(context: Any?): SoundPlayer {
    return PlatformSoundPlayer(context as Context)
}
```

**iOS Implementation**:
```kotlin
actual fun createSoundPlayer(context: Any?): SoundPlayer {
    return PlatformSoundPlayer() // Stub
}
```

### 3. MVVM with Shared ViewModel
```kotlin
class ClapViewModel(private val soundPlayer: SoundPlayer) : ViewModel() {
    private val _clapCount = MutableStateFlow(0)
    val clapCount: StateFlow<Int> = _clapCount

    fun onClapRequested() {
        soundPlayer.playClap()
        _clapCount.value++
    }
}
```

## Key Architectural Decisions

### Why expect/actual Functions Instead of Classes?
- **Simpler**: Less boilerplate than expect/actual classes
- **Better Swift Interop**: Functions work better with Swift bridging
- **Koin Compatibility**: Easier to integrate with Koin modules

### Why Swift AudioPlayer for iOS?
- **Complexity**: AVFoundation Kotlin/Native interop is challenging
- **Pragmatic**: Swift has first-class AVFoundation support
- **Maintainability**: Native Swift code is easier for iOS developers

### Why Static Framework for iOS?
- **Koin Requirement**: Koin needs static framework for iOS
- **Configuration**: `isStatic = true` in shared/build.gradle.kts

### Why Product Flavors?
- **Parallel Testing**: Install dev and prod versions simultaneously
- **Testing**: Test migrations without uninstalling production app

## Build Commands

### Android
```bash
# Build debug APK (dev flavor)
./gradlew :app:assembleDevDebug

# Build debug APK (prod flavor)
./gradlew :app:assembleProdDebug

# Install dev debug to device
./gradlew :app:installDevDebug

# Clean build
./gradlew clean
```

### iOS
```bash
# Build shared framework for simulator
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Build for device
./gradlew :shared:linkDebugFrameworkIosArm64
```

## Common Issues & Solutions

### Issue: Android app crashes on launch
**Symptom**: NullPointerException or Koin not initialized
**Solution**: Ensure `ClapApplication` is registered in AndroidManifest.xml
**Check**: `android:name=".ClapApplication"` in `<application>` tag

### Issue: iOS cannot find KoinHelper
**Symptom**: Build error or runtime crash
**Solution**: Ensure shared framework is static
**Check**: `isStatic = true` in `shared/build.gradle.kts`

### Issue: Audio not playing on iOS
**Symptom**: No sound when tapping
**Solution**: Verify claps.wav is in Xcode project
**Check**: Xcode project navigator and "Copy Bundle Resources" build phase

### Issue: Gradle sync fails
**Symptom**: Version conflicts or missing dependencies
**Solution**: Clean Gradle cache and re-sync
**Commands**:
```bash
./gradlew clean
rm -rf .gradle
./gradlew --refresh-dependencies
```

### Issue: Xcode user files in git
**Symptom**: xcuserstate or xcuserdata in git status
**Solution**: Already handled in .gitignore (as of this session)
**Ignored**: `.kotlin/`, `*.xcuserstate`, `*.xcuserdatad/`, `xcuserdata/`

## Testing Status
- Android: Tested on Samsung Galaxy Z Flip 5 (physical device)
- iOS: Tested on iPhone 16 Pro Simulator
- DI: Koin working on both platforms
- Audio: Functional on both platforms

## Git Workflow

### Current Branch
`kmp-migration` (ahead of origin by 1 commit from start of this session)

### Main Branch
`master`

### Recent Commits
- `9d6c3ba` - docs: Add WARP.md configuration file
- `ff33789` - KMP migration
- `94c8197` - Introducing script for automatic publishing

### Next Steps
1. Merge `kmp-migration` into `master`
2. Tag release (v1.1.0)
3. Consider adding CI/CD

## Session Work Summary (2025-10-29)

### Android Platform Fixes
1. Created `ClapApplication.kt` for Koin initialization
2. Updated `AndroidManifest.xml` to register Application class
3. Modified `app/build.gradle.kts` with Koin dependency + product flavors
4. Updated `MainActivity.kt` to use `by inject()`
5. Fixed Koin imports in `PlatformSoundPlayer.kt`
6. Created `AndroidModule.kt` for Android-specific Koin configuration
7. Created `ResourceReader.kt` for resource access
8. Confirmed `claps.ogg` audio resource

### iOS Platform Fixes
1. Created `KoinHelper.kt` for Swift-Kotlin bridge
2. Created `IosModule.kt` for iOS Koin module
3. Updated `PlatformSoundPlayer.kt` with documentation
4. Modified `iosAppApp.swift` to initialize Koin
5. Updated `ContentView.swift` to use KoinHelper + AudioPlayer
6. Added `AudioPlayer.swift` (Swift audio implementation)
7. Converted and added `claps.wav` (942KB)
8. Created `README_AUDIO.md` documentation
9. Updated Xcode project to include audio file
10. Configured static framework in `shared/build.gradle.kts`

### Shared Module Updates
1. Created `KoinSetup.kt` for common Koin setup
2. Updated `ClapViewModel.kt` with constructor injection
3. Updated `SoundPlayer.kt` with expect function pattern
4. Created `shared/src/commonMain/resources/` directory
5. Deleted platform-specific ViewModels (unified in commonMain)
6. Moved SoundPlayer from app to shared module

### Documentation Updates
1. Updated `readme.md` with complete Koin architecture
2. Updated `MIGRATION_GUIDE.md` with accurate implementation guide
3. Updated `WARP.md` for AI tool guidance

### Build Configuration
1. Updated root `build.gradle.kts`
2. Updated `gradle.properties`
3. Updated `gradle/libs.versions.toml` (Kotlin 2.1.21, Koin 4.0.0)
4. Updated Gradle wrapper to 8.5
5. Updated `settings.gradle.kts`

### Product Flavors (New)
- dev: `pe.devpicon.android.clapapp.dev`
- prod: `pe.devpicon.android.clapapp`
- Version: 1.1.0-dev (versionCode 7)

### Testing Completed
- Android dev flavor on Samsung Galaxy Z Flip 5
- iOS on iPhone 16 Pro Simulator
- Koin DI on both platforms
- All builds successful

## Dependencies Reference

### Kotlin & Gradle
- Kotlin: 2.1.21
- Gradle: 8.5
- Android Gradle Plugin: 8.1.4

### Koin
- koin-core: 4.0.0
- koin-android: 4.0.0

### Android
- Compose BOM: 2025.01.00
- Activity Compose: 1.9.3
- Core KTX: 1.15.0

### Build Configuration
Version catalog location: `gradle/libs.versions.toml`

## Code Snippets for Common Tasks

### Adding a New Dependency to Shared Module
```kotlin
// shared/build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dependency:here:version")
        }
    }
}
```

### Adding Android-Specific Dependency
```kotlin
// app/build.gradle.kts
dependencies {
    implementation("dependency:here:version")
}
```

### Creating a New Platform Function
```kotlin
// commonMain
expect fun newPlatformFunction(): String

// androidMain
actual fun newPlatformFunction(): String = "Android"

// iosMain
actual fun newPlatformFunction(): String = "iOS"
```

### Adding to Koin Module
```kotlin
// AndroidModule.kt
val androidModule = module {
    single { NewDependency() }
}
```

## Important Notes for AI Assistants

1. **Don't Modify Xcode User Files**: Files in `xcuserdata/` are user-specific
2. **Static Framework Required**: iOS needs static framework for Koin
3. **Separate Audio Files**: Android uses OGG, iOS uses WAV
4. **Product Flavors**: Use `dev` flavor for testing, `prod` for release
5. **Koin Initialization**: Must happen in Application class (Android) or App struct (iOS)
6. **Swift Integration**: Use KoinHelper for accessing Koin from Swift
7. **expect/actual**: Use functions, not classes, for simpler interop

## Future Enhancements

### Short Term
1. Add unit tests for ClapViewModel
2. Add UI tests for both platforms
3. Improve error handling

### Medium Term
1. Explore shared audio resources
2. Investigate kotlinx-media or similar for unified audio
3. Add analytics

### Long Term
1. CI/CD pipeline (GitHub Actions)
2. Automated testing
3. Performance monitoring
4. Crashlytics integration

## Helpful Resources
- Koin Docs: https://insert-koin.io/
- KMP Docs: https://kotlinlang.org/docs/multiplatform.html
- Compose Docs: https://developer.android.com/jetpack/compose
- SwiftUI Docs: https://developer.apple.com/xcode/swiftui/

## Session Closure Notes (2025-10-29)
- All changes tested and working
- Documentation fully updated
- Context files created for AI assistants (Claude.md, Gemini.md, Agents.md)
- Ready for commit and push
- Next action: Merge to master and create release tag
