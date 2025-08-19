package pe.devpicon.clapapp.shared

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class PlatformSoundPlayer : SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    init {
        try {
            // Note: This will need to be initialized with a context
            // We'll handle this through a factory method or dependency injection
        } catch (e: Exception) {
            Log.e("PlatformSoundPlayer", "Error creating MediaPlayer: ${e.message}", e)
            mediaPlayer = null
        }
    }

    fun initialize(context: Context) {
        try {
            mediaPlayer = MediaPlayer.create(context, getClapSoundResourceId(context))
            
            if (mediaPlayer == null) {
                Log.e("PlatformSoundPlayer", "MediaPlayer.create returned null. Check your sound resource.")
            }

            mediaPlayer?.setOnErrorListener { _, what, extra ->
                Log.e("PlatformSoundPlayer", "MediaPlayer error: what $what, extra $extra")
                _isPlaying.value = false
                true
            }
            
            mediaPlayer?.setOnCompletionListener {
                _isPlaying.value = false
            }
        } catch (e: Exception) {
            Log.e("PlatformSoundPlayer", "Error creating MediaPlayer: ${e.message}", e)
            mediaPlayer = null
        }
    }

    override fun playClapSound() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                }
                player.seekTo(0)
                player.start()
                _isPlaying.value = true
            } catch (e: IllegalStateException) {
                Log.e("PlatformSoundPlayer", "Error playing sound: ${e.message}", e)
                try {
                    mediaPlayer?.release()
                    // Re-create the MediaPlayer - this would need context
                    // For now, we'll just log the error
                    _isPlaying.value = false
                } catch (e2: Exception) {
                    Log.e("PlatformSoundPlayer", "Error recovering MediaPlayer: ${e2.message}", e2)
                }
            }
        } ?: Log.e("PlatformSoundPlayer", "MediaPlayer is null, cannot play sound.")
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        Log.d("PlatformSoundPlayer", "MediaPlayer released")
    }
    
    private fun getClapSoundResourceId(context: Context): Int {
        return context.resources.getIdentifier("claps", "raw", context.packageName)
    }
}
