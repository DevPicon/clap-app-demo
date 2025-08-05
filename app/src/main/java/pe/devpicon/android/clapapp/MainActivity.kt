package pe.devpicon.android.clapapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pe.devpicon.android.clapapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var soundPlayer: SoundPlayer
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        soundPlayer = SoundPlayer(this)

        binding.btnLaunch.setOnClickListener {
            soundPlayer.playClapSound()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.release() // Release MediaPlayer resources
    }
}
