package com.doug2d2.chore_divvy_android

import android.app.Application
import timber.log.Timber

class ChoreDivvyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}