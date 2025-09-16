package com.artiusid.sample

import android.app.Application
import android.os.Build
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.util.DebugLogger
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

/**
 * Sample Application with Hilt Support
 * 
 * Provides Hilt dependency injection for both the sample app
 * and the embedded SDK standalone application.
 */
@HiltAndroidApp
class SampleApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d("SampleApplication", "🚀 Starting SampleApplication initialization...")
        
        // Initialize Firebase (critical for FCM tokens)
        try {
            FirebaseApp.initializeApp(this)
            Log.d("SampleApplication", "🔥 Firebase initialized successfully in sample app")
        } catch (e: Exception) {
            Log.e("SampleApplication", "❌ Firebase initialization failed", e)
        }
        
        Log.d("SampleApplication", "✅ SampleApplication onCreate completed")
    }
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // Add GIF support (matching standalone app)
                add(GifDecoder.Factory())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                }
            }
            .okHttpClient {
                OkHttpClient.Builder().build()
            }
            .logger(DebugLogger()) // Enable logging for debugging
            .build()
    }
}