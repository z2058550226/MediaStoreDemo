package com.bybutter.mediatest

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.suika.astree.AndroidStudioTree
import timber.log.Timber

lateinit var app: App

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        app = this
        Timber.plant(AndroidStudioTree())
        Fresco.initialize(this)
    }
}