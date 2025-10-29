# iOS Audio Setup

## Current Status

✅ **Audio file ready**: `claps.wav` (942KB) - converted from OGG format
⚠️ **Implementation**: Stub (pending AVFoundation platform interop fix)

## What's Ready

The audio file has been:
1. ✅ Converted from OGG to WAV format (iOS native support)
2. ✅ Placed in `iosApp/iosApp/claps.wav`
3. ✅ Implementation code documented in `PlatformSoundPlayer.kt`

## Next Steps to Enable Audio

### Option 1: Add to Xcode Project (Manual)

1. Open `iosApp.xcodeproj` in Xcode
2. Right-click on `iosApp` folder → "Add Files to 'iosApp'..."
3. Select `claps.wav`
4. Check ☑️ "Copy items if needed"
5. Check ☑️ "Add to target: iosApp"
6. Click "Add"

### Option 2: Fix AVFoundation Interop (Requires Research)

The Kotlin/Native compiler currently cannot resolve AVFoundation imports. This is a platform interop configuration issue with Kotlin 2.1.21.

**Possible solutions:**
- Create `.def` file for AVFoundation cinterop
- Add cinterop configuration in `shared/build.gradle.kts`
- Use Swift wrapper to access AVAudioPlayer
- Downgrade Kotlin to version with working AVFoundation support

**Implementation code is ready** (see `PlatformSoundPlayer.kt` comments):
```kotlin
import platform.AVFoundation.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

private var player: AVAudioPlayer? = null
init {
    val path = NSBundle.mainBundle.pathForResource("claps", "wav")
    if (path != null) {
        player = AVAudioPlayer(NSURL.fileURLWithPath(path), null)
        player?.prepareToPlay()
    }
}
override fun playClapSound() {
    player?.currentTime = 0.0
    player?.play()
    _isPlaying.value = true
}
```

## File Information

**File**: `claps.wav`
**Size**: 942KB
**Format**: WAV (16-bit PCM)
**Source**: Converted from `shared/src/commonMain/resources/claps.ogg`
**Conversion**: macOS `afconvert` tool

## Testing

Once the file is added to Xcode and AVFoundation interop is fixed:
1. Rebuild the app
2. Run on simulator
3. Tap the screen to trigger audio playback
4. Audio should play through device speakers
