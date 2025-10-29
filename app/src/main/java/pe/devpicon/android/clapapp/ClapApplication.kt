package pe.devpicon.android.clapapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pe.devpicon.clapapp.shared.getPlatformModule

class ClapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ClapApplication)
            modules(getPlatformModule())
        }
    }
}
