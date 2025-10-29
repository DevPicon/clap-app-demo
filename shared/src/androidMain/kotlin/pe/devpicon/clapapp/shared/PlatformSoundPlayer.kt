package pe.devpicon.clapapp.shared

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun buildPlatformSoundPlayer(): SoundPlayer = object : KoinComponent {}.get()

class AndroidSoundPlayer(private val resourceReader: ResourceReader) : SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)

    override val isPlaying: StateFlow<Boolean> = _isPlaying

    init {
        try {
            val resourceId = resourceReader.getRawResourceId("claps")
            mediaPlayer = MediaPlayer.create(resourceReader.context, resourceId)
            
            if (mediaPlayer == null) {
                Log.e("PlatformSoundPlayer", "MediaPlayer factory returned null. Check your resource.")
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
                mediaPlayer?.release()
                val resourceId = resourceReader.getRawResourceId("claps")
                mediaPlayer = MediaPlayer.create(resourceReader.context, resourceId)
                mediaPlayer?.start()
                _isPlaying.value = mediaPlayer != null
            }
        } ?: run {
            Log.e("PlatformSoundPlayer", "MediaPlayer is null, attempting to (re)create via factory.")
            val resourceId = resourceReader.getRawResourceId("claps")
            mediaPlayer = MediaPlayer.create(resourceReader.context, resourceId)
            mediaPlayer?.start()
            _isPlaying.value = mediaPlayer != null
        }
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        Log.d("PlatformSoundPlayer", "MediaPlayer released")
    }
}
