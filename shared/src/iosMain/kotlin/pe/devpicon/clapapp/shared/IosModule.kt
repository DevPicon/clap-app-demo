package pe.devpicon.clapapp.shared

import org.koin.dsl.module

val iosModule = module {
    single<SoundPlayer> { NativeSoundPlayer() }
    single { ClapViewModel(get()) }
}

actual fun getPlatformModule() = iosModule
