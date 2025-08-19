package pe.devpicon.clapapp.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

expect class ClapViewModel() {
    val clapCount: StateFlow<Int>
    val isPlaying: StateFlow<Boolean>
    fun onClapClick()
    fun initialize()
    fun release()
}
