package com.artiusid.sample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Sample Application with Hilt Support
 * 
 * Provides Hilt dependency injection for both the sample app
 * and the embedded SDK standalone application.
 */
@HiltAndroidApp
class SampleApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization
    }
}