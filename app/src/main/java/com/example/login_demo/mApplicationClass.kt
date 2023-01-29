package com.example.login_demo

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class mApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()


        AppEventsLogger.activateApp(this);
    }
}