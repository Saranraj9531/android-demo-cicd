package com.gps.chat.utils

import android.app.Application
import com.sparkout.chat.common.ChatApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : ChatApp() {

    companion object {
        lateinit var mInstance: App
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this

    }
}