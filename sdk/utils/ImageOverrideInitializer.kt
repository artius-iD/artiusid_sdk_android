/*
 * File: ImageOverrideInitializer.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.util.DebugLogger
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.models.SDKImageOverrides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Initializer for the Image Override System
 * Handles setup of ImageOverrideManager and enhanced Coil configuration
 */
object ImageOverrideInitializer {
    private const val TAG = "ImageOverrideInitializer"
    
    /**
     * Initialize the image override system with SDK configuration
     */
    fun initialize(context: Context, sdkConfiguration: SDKConfiguration) {
        Log.d(TAG, "Initializing image override system...")
        
        try {
            // Create enhanced ImageLoader for override support
            val imageLoader = createEnhancedImageLoader(context, sdkConfiguration.imageOverrides)
            
            // Initialize the ImageOverrideManager
            val manager = ImageOverrideManager.initialize(
                context = context,
                overrides = sdkConfiguration.imageOverrides,
                imageLoader = imageLoader
            )
            
            // Preload images if configured
            if (sdkConfiguration.imageOverrides.preloadImages) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        manager.preloadImages()
                        Log.d(TAG, "Image preloading completed successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during image preloading", e)
                    }
                }
            }
            
            Log.d(TAG, "Image override system initialized successfully")
            logOverrideConfiguration(sdkConfiguration.imageOverrides)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize image override system", e)
            throw e
        }
    }
    
    /**
     * Create an enhanced ImageLoader with optimized configuration for overrides
     */
    private fun createEnhancedImageLoader(context: Context, overrides: SDKImageOverrides): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                // Add GIF support for animated overrides
                add(GifDecoder.Factory())
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                }
            }
            // Don't override OkHttpClient - use the default one to avoid mTLS conflicts
            // .okHttpClient { ... } - removed to prevent conflicts with mTLS setup
            .memoryCache {
                coil.memory.MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_overrides"))
                    .maxSizePercent(0.02) // Use 2% of available disk space
                    .build()
            }
            .respectCacheHeaders(false) // For better control over caching
            .allowHardware(true) // Enable hardware bitmaps for better performance
            .crossfade(true) // Smooth transitions
            .logger(if (android.util.Log.isLoggable(TAG, android.util.Log.DEBUG)) DebugLogger() else null)
            .build()
    }
    
    /**
     * Log the current override configuration for debugging
     */
    private fun logOverrideConfiguration(overrides: SDKImageOverrides) {
        if (!Log.isLoggable(TAG, Log.DEBUG)) return
        
        Log.d(TAG, "=== Image Override Configuration ===")
        Log.d(TAG, "Loading Strategy: ${overrides.defaultLoadingStrategy}")
        Log.d(TAG, "Caching Enabled: ${overrides.enableCaching}")
        Log.d(TAG, "Cache Duration: ${overrides.cacheDurationMs}ms")
        Log.d(TAG, "Fallback Enabled: ${overrides.enableFallback}")
        Log.d(TAG, "Preload Images: ${overrides.preloadImages}")
        
        // Log specific overrides
        val activeOverrides = mutableListOf<String>()
        
        if (overrides.faceOverlay != null) activeOverrides.add("face_overlay")
        if (overrides.faceUpGif != null) activeOverrides.add("face_up_gif")
        if (overrides.faceDownGif != null) activeOverrides.add("face_down_gif")
        if (overrides.phoneUpGif != null) activeOverrides.add("phone_up_gif")
        if (overrides.phoneDownGif != null) activeOverrides.add("phone_down_gif")
        if (overrides.passportOverlay != null) activeOverrides.add("passport_overlay")
        if (overrides.stateIdFrontOverlay != null) activeOverrides.add("state_id_front_overlay")
        if (overrides.stateIdBackOverlay != null) activeOverrides.add("state_id_back_overlay")
        if (overrides.brandLogo != null) activeOverrides.add("brand_logo")
        if (overrides.successIcon != null) activeOverrides.add("success_icon")
        if (overrides.failedIcon != null) activeOverrides.add("failed_icon")
        if (overrides.backButtonIcon != null) activeOverrides.add("back_button_icon")
        
        activeOverrides.addAll(overrides.customOverrides.keys)
        
        Log.d(TAG, "Active Overrides (${activeOverrides.size}): ${activeOverrides.joinToString(", ")}")
        
        if (overrides.customOverrides.isNotEmpty()) {
            Log.d(TAG, "Custom Overrides:")
            overrides.customOverrides.forEach { (key, value) ->
                Log.d(TAG, "  $key -> $value")
            }
        }
        
        Log.d(TAG, "=== End Override Configuration ===")
    }
    
    /**
     * Clean up resources when SDK is destroyed
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up image override system...")
        
        try {
            if (ImageOverrideManager.isInitialized()) {
                val manager = ImageOverrideManager.getInstance()
                manager.clearCache()
                
                val stats = manager.getCacheStats()
                Log.d(TAG, "Final cache stats: $stats")
            }
            
            Log.d(TAG, "Image override system cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during image override system cleanup", e)
        }
    }
    
    /**
     * Get current override statistics for debugging/monitoring
     */
    fun getOverrideStats(): Map<String, Any> {
        return try {
            if (ImageOverrideManager.isInitialized()) {
                val manager = ImageOverrideManager.getInstance()
                mapOf(
                    "initialized" to true,
                    "cacheStats" to manager.getCacheStats()
                )
            } else {
                mapOf("initialized" to false)
            }
        } catch (e: Exception) {
            mapOf(
                "initialized" to false,
                "error" to (e.message ?: "Unknown error")
            )
        }
    }
}
