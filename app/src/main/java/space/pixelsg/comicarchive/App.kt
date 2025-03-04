package space.pixelsg.comicarchive

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import space.pixelsg.comicarchive.data.di.dataModule
import space.pixelsg.comicarchive.service.di.serviceModule
import space.pixelsg.comicarchive.ui.di.viewModelsModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                dataModule(),
                serviceModule(),
                viewModelsModule(),
            )
        }
    }
}