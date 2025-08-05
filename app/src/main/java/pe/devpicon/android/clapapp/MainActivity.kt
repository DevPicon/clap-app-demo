package pe.devpicon.android.clapapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pe.devpicon.android.clapapp.ui.compose.MainScreen // Import the new Composable

// Removed unused imports like android.widget.ImageButton if they were solely for the XML.
// Ensure androidx.compose.material3.Text is imported if not automatically,
// though MainScreen.kt handles its own imports.

class MainActivity : ComponentActivity() { // Changed from AppCompatActivity
    private lateinit var soundPlayer: SoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundPlayer = SoundPlayer(this) // Uses R.raw.claps

        setContent {
            // If you have a global Compose theme (e.g., in ui.theme.Theme.kt), wrap MainScreen with it.
            // For example:
            // ClapAppDemoTheme { // Replace with your actual theme name from your project
            //    MainScreen(onLaunchClick = {
            //        soundPlayer.playClapSound()
            //    })
            // }

            // Calling MainScreen directly if no specific app theme is set up for this example
            MainScreen(onLaunchClick = {
                soundPlayer.playClapSound()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.release()
    }
}
