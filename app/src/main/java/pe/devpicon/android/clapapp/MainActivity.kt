package pe.devpicon.android.clapapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import pe.devpicon.android.clapapp.ui.compose.MainScreen // Import the new Composable

// Removed unused imports like android.widget.ImageButton if they were solely for the XML.
// Ensure androidx.compose.material3.Text is imported if not automatically,
// though MainScreen.kt handles its own imports.

class MainActivity : ComponentActivity() { // Changed from AppCompatActivity
    private lateinit var soundPlayer: SoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        soundPlayer = SoundPlayer(this) // Uses R.raw.claps

        setContent {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            val versionCode = packageManager.getPackageInfo(packageName, 0).versionCode
            val versionInfo = "${versionName}v-$versionCode"

            MainScreen(
                onLaunchClick = {
                    soundPlayer.playClapSound()
                },
                versionInfo = versionInfo
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.release()
    }
}
