package pe.devpicon.clapapp.shared

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin

/**
 * Helper class for iOS to interact with Koin dependency injection.
 * iOS SwiftUI code can use this to initialize Koin and retrieve dependencies.
 */
class KoinHelper : KoinComponent {

    companion object {
        /**
         * Initialize Koin with iOS platform module.
         * Call this once in your iOS app initialization (e.g., in App init or AppDelegate).
         */
        fun doInitKoin() {
            startKoin {
                modules(getPlatformModule())
            }
        }
    }

    /**
     * Get ClapViewModel instance from Koin.
     * Usage from Swift: KoinHelper().getClapViewModel()
     */
    fun getClapViewModel(): ClapViewModel = get()

    /**
     * Get SoundPlayer instance from Koin.
     * Usage from Swift: KoinHelper().getSoundPlayer()
     */
    fun getSoundPlayer(): SoundPlayer = get()
}
