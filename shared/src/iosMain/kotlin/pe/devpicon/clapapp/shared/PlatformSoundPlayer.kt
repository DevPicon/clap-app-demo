package pe.devpicon.clapapp.shared

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun buildPlatformSoundPlayer(): SoundPlayer = object : KoinComponent {}.get()

/**
 * Native iOS SoundPlayer implementation.
 *
 * STATUS: Stub implementation - Audio file ready but AVFoundation interop pending
 *
 * READY FOR USE:
 * - claps.wav file converted and available at: iosApp/iosApp/claps.wav (942KB)
 * - File needs to be added to Xcode project (Add Files to "iosApp"...)
 * - Check "Copy items if needed" and "Add to target: iosApp"
 *
 * TODO: Fix AVFoundation platform interop configuration
 * - Requires .def file or cinterop configuration in build.gradle.kts
 * - Or use Swift wrapper to access AVAudioPlayer
 *
 * IMPLEMENTATION READY (pending interop fix):
 * ```kotlin
 * import platform.AVFoundation.AVAudioPlayer
 * import platform.Foundation.NSBundle
 * import platform.Foundation.NSURL
 *
 * private var player: AVAudioPlayer? = null
 * init {
 *     val path = NSBundle.mainBundle.pathForResource("claps", "wav")
 *     if (path != null) {
 *         player = AVAudioPlayer(NSURL.fileURLWithPath(path), null)
 *         player?.prepareToPlay()
 *     }
 * }
 * override fun playClapSound() {
 *     player?.currentTime = 0.0
 *     player?.play()
 *     _isPlaying.value = true
 * }
 * ```
 */
class NativeSoundPlayer : SoundPlayer {
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    init {
        println("NativeSoundPlayer: Initialized (stub - claps.wav ready in iosApp folder)")
    }

    override fun playClapSound() {
        println("NativeSoundPlayer: playClapSound called (stub - audio file ready but AVFoundation pending)")
        _isPlaying.value = true
        // Simulate playback completion
        GlobalScope.launch {
            delay(1000)
            _isPlaying.value = false
        }
    }

    override fun release() {
        println("NativeSoundPlayer: Released")
        _isPlaying.value = false
    }
}
