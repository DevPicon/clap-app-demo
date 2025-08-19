package pe.devpicon.clapapp.shared

import kotlinx.coroutines.flow.StateFlow

interface SoundPlayer {
    fun playClapSound()
    fun release()
    val isPlaying: StateFlow<Boolean>
}

expect class PlatformSoundPlayer() : SoundPlayer
