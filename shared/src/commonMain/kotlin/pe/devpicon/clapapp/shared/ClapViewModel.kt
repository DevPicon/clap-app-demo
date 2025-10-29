package pe.devpicon.clapapp.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClapViewModel(private val soundPlayer: SoundPlayer) {
    private val _clapCount = MutableStateFlow(0)
    val clapCount: StateFlow<Int> = _clapCount.asStateFlow()
    
    val isPlaying: StateFlow<Boolean> = soundPlayer.isPlaying
    
    fun onClapClick() {
        soundPlayer.playClapSound()
        _clapCount.value += 1
    }
    
    fun initialize() {
        // Platform-specific initialization is handled by SoundPlayer implementations
    }
    
    fun release() {
        soundPlayer.release()
    }
}
