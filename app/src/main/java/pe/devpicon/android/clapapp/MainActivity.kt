package pe.devpicon.android.clapapp

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pe.devpicon.android.clapapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var clapSound: MediaPlayer
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (!this::clapSound.isInitialized) {
            clapSound = MediaPlayer.create(this, R.raw.claps);
        }

        binding.btnLaunch.setOnClickListener {
            clapSound.start()
        }
    }
}
