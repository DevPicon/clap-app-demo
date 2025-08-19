package pe.devpicon.clapapp.shared

import android.content.Context
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
    
    fun initialize(context: Context) {
        soundPlayer.initialize(context)
    }
    
    actual fun initialize() {
        // This will be called from common code, but we need context
        // We'll handle this through a different approach
    }
    
    actual fun release() {
        soundPlayer.release()
    }
}
