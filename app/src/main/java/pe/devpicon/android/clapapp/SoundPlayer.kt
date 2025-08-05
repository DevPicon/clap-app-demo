package pe.devpicon.android.clapapp

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

class SoundPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    init {
        // Initialize the MediaPlayer.
        // IMPORTANT: Replace R.raw.claps with your actual sound resource ID.
        // Ensure you have a sound file (e.g., claps.ogg)
        // in your res/raw directory.
        try {
            // Using applicationContext to avoid leaking Activity context if SoundPlayer's
            // lifecycle was to become longer than the Activity's.
            // However, for MediaPlayer.create, an Activity context is often used.
            // Given our current design (SoundPlayer owned by Activity and released in onDestroy),
            // using the passed 'context' directly is also acceptable.
            // Let's stick to the passed context for now as it's simpler and common.
            mediaPlayer = MediaPlayer.create(context, R.raw.claps) // UPDATED to R.raw.claps

            if (mediaPlayer == null) {
                Log.e("SoundPlayer", "MediaPlayer.create returned null. Check your sound resource.")
            }

            mediaPlayer?.setOnErrorListener { _, what, extra ->
                Log.e("SoundPlayer", "MediaPlayer error: what $what, extra $extra")
                // true indicates the error was handled
                true
            }
        } catch (e: Exception) {
            // This can happen if the resource ID is invalid or the file is corrupt
            Log.e("SoundPlayer", "Error creating MediaPlayer: ${e.message}", e)
            mediaPlayer = null
        }
    }

    fun playClapSound() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause() // Pause if already playing
                }
                player.seekTo(0) // Go to the beginning
                player.start()   // Start playback
            } catch (e: IllegalStateException) {
                Log.e("SoundPlayer", "Error playing sound: ${e.message}", e)
                // As a fallback, try to release and re-create the MediaPlayer
                // This can help if the player gets into an unrecoverable state.
                try {
                    mediaPlayer?.release()
                    // Ensure R.raw.claps is your correct sound resource ID
                    mediaPlayer = MediaPlayer.create(context, R.raw.claps)
                    mediaPlayer?.start()
                } catch (e2: Exception) {
                    Log.e("SoundPlayer", "Error recovering MediaPlayer: ${e2.message}", e2)
                }
            }
        } ?: Log.e("SoundPlayer", "MediaPlayer is null, cannot play sound.")
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null // Clear the reference to allow GC and prevent further use
        Log.d("SoundPlayer", "MediaPlayer released")
    }
}
