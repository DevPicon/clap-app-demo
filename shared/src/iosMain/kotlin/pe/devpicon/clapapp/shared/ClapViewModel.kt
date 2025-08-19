package pe.devpicon.clapapp.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class ClapViewModel {
    private val soundPlayer = PlatformSoundPlayer()
    
    private val _clapCount = MutableStateFlow(0)
    actual val clapCount: StateFlow<Int> = _clapCount.asStateFlow()
    
    actual val isPlaying: StateFlow<Boolean> = soundPlayer.isPlaying
    
    actual fun onClapClick() {
        soundPlayer.playClapSound()
        _clapCount.value += 1
    }
    
    actual fun initialize() {
        // iOS initialization is handled in PlatformSoundPlayer constructor
    }
    
    actual fun release() {
        soundPlayer.release()
    }
}
