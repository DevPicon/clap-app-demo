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
        if (mediaPlayer == null) {
            Log.e("SoundPlayer", "MediaPlayer not initialized or failed to initialize. Cannot play sound.")
            return
        }

        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                // You need to call prepare() or prepareAsync() after stop() before you can play again.
                // Since MediaPlayer.create prepares the source, we might need to reset and set data source again
                // or re-create it if we want to play from the beginning.
                // For simplicity, let's re-create it or simply seek to the beginning if already prepared.
                // A simpler approach for short sounds: if playing, stop and start again.
                // However, MediaPlayer.create handles prepare internally.
                // To play again from the start if it was already playing:
                mediaPlayer?.seekTo(0)
            }
            mediaPlayer?.start()
        } catch (e: IllegalStateException) {
            Log.e("SoundPlayer", "Error playing sound: ${e.message}", e)
            // It might be good to try and recover or re-initialize mediaPlayer here
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null // Clear the reference to allow GC and prevent further use
        Log.d("SoundPlayer", "MediaPlayer released")
    }
}
