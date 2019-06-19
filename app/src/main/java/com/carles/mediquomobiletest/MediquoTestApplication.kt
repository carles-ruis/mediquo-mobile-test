package com.carles.mediquomobiletest

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MediquoTestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MediquoTestApplication)
            modules(appModule)
        }
    }

}