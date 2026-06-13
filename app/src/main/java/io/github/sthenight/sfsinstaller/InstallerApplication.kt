package io.github.sthenight.sfsinstaller

import android.app.Application
import io.github.sthenight.sfsinstaller.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class InstallerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@InstallerApplication)
            modules(appModule)
        }
    }
}
