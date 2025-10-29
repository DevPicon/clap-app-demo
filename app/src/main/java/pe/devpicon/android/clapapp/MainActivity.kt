package pe.devpicon.android.clapapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.android.ext.android.inject
import pe.devpicon.android.clapapp.ui.compose.MainScreen
import pe.devpicon.clapapp.shared.ClapViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ClapViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen(
                onLaunchClick = { viewModel.onClapClick() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }
}
