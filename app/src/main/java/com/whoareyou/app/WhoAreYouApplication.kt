package com.whoareyou.app

import android.app.Application

class WhoAreYouApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppEvents.configure()
    }
}
