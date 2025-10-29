package pe.devpicon.clapapp.shared

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single { ResourceReader(androidContext()) }
    single<SoundPlayer> { AndroidSoundPlayer(get()) }
    single { ClapViewModel(get()) }
}

actual fun getPlatformModule() = androidModule
