package pe.devpicon.clapapp.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

actual class PlatformSoundPlayer : SoundPlayer {
    private val _isPlaying = MutableStateFlow(false)
    
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    override fun playClapSound() {
        // TODO: Implementar reproducción de audio nativa para iOS
        // Por ahora solo simulamos el comportamiento
        println("iOS: Playing clap sound")
        _isPlaying.value = true
        
        // Simular que el sonido termina después de un tiempo
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(1000)
            _isPlaying.value = false
        }
    }

    override fun release() {
        _isPlaying.value = false
        println("iOS: AudioPlayer released")
    }
}
