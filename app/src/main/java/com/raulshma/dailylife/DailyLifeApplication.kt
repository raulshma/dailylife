package com.raulshma.dailylife

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DailyLifeApplication : Application() {
    override fun onCreate() {
        System.loadLibrary("sqlcipher")
        super.onCreate()
    }
}
