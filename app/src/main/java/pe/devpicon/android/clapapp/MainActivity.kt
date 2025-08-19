package pe.devpicon.android.clapapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pe.devpicon.android.clapapp.ui.compose.MainScreen
import pe.devpicon.clapapp.shared.ClapViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: ClapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = ClapViewModel()
        viewModel.initialize(this)

        setContent {
            MainScreen(
                onLaunchClick = {
                    viewModel.onClapClick()
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }
}
