package pe.devpicon.android.clapapp

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var clapSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!this::clapSound.isInitialized) {
            clapSound = MediaPlayer.create(this, R.raw.claps);
        }

        btn_launch.setOnClickListener {
            clapSound.start()
        }
    }
}
