/*
 * File: ImageOverrideManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.artiusid.sdk.models.SDKImageOverrides
import com.artiusid.sdk.models.ImageLoadingStrategy
import com.artiusid.sdk.models.ImageOverrideResult
import com.artiusid.sdk.models.hasOverride
import com.artiusid.sdk.models.getOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized Image Override Manager
 * Handles resolution, loading, and caching of override images and GIFs
 */
class ImageOverrideManager private constructor(
    private val context: Context,
    private val overrides: SDKImageOverrides,
    private val imageLoader: ImageLoader
) {
    companion object {
        private const val TAG = "ImageOverrideManager"
        
        @Volatile
        private var INSTANCE: ImageOverrideManager? = null
        
        /**
         * Initialize the ImageOverrideManager singleton
         */
        fun initialize(
            context: Context, 
            overrides: SDKImageOverrides,
            imageLoader: ImageLoader? = null
        ): ImageOverrideManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageOverrideManager(
                    context.applicationContext,
                    overrides,
                    imageLoader ?: ImageLoader(context)
                ).also { 
                    INSTANCE = it
                    Log.d(TAG, "ImageOverrideManager initialized with ${overrides.customOverrides.size} custom overrides")
                }
            }
        }
        
        /**
         * Get the current instance (must be initialized first)
         */
        fun getInstance(): ImageOverrideManager {
            return INSTANCE ?: throw IllegalStateException("ImageOverrideManager not initialized. Call initialize() first.")
        }
        
        /**
         * Check if manager is initialized
         */
        fun isInitialized(): Boolean = INSTANCE != null
    }
    
    // Cache for resolved image sources
    private val imageCache = ConcurrentHashMap<String, ImageOverrideResult>()
    
    // Cache for loaded drawables (memory cache)
    private val drawableCache = ConcurrentHashMap<String, Drawable>()
    
    /**
     * Resolve image source for a given default resource and override key
     * Returns the appropriate source (URL, resource ID, file path, etc.)
     */
    fun resolveImageSource(@DrawableRes defaultResourceId: Int, overrideKey: String): ImageOverrideResult {
        // Check cache first
        val cacheKey = "${overrideKey}_${defaultResourceId}"
        imageCache[cacheKey]?.let { cached ->
            Log.d(TAG, "Using cached image source for key: $overrideKey")
            return cached
        }
        
        // Check if override exists
        val overrideValue = overrides.getOverride(overrideKey)
        
        val result = if (overrideValue != null) {
            Log.d(TAG, "Found override for key '$overrideKey': $overrideValue")
            resolveOverrideSource(overrideValue, overrides.defaultLoadingStrategy)
        } else {
            Log.d(TAG, "No override found for key '$overrideKey', using default resource: $defaultResourceId")
            ImageOverrideResult(
                source = defaultResourceId,
                strategy = ImageLoadingStrategy.RESOURCE,
                isFallback = false
            )
        }
        
        // Cache the result if caching is enabled
        if (overrides.enableCaching) {
            imageCache[cacheKey] = result
        }
        
        return result
    }
    
    /**
     * Resolve GIF source for animated content
     */
    fun resolveGifSource(@RawRes defaultResourceId: Int, overrideKey: String): ImageOverrideResult {
        return resolveImageSource(defaultResourceId, overrideKey)
    }
    
    /**
     * Preload images if configured to do so
     */
    suspend fun preloadImages() {
        if (!overrides.preloadImages) {
            Log.d(TAG, "Image preloading disabled")
            return
        }
        
        Log.d(TAG, "Starting image preloading...")
        
        withContext(Dispatchers.IO) {
            val preloadKeys = listOf(
                "face_overlay", "passport_overlay", "state_id_front_overlay", "state_id_back_overlay",
                "brand_logo", "success_icon", "failed_icon", "back_button_icon"
            )
            
            preloadKeys.forEach { key ->
                if (overrides.hasOverride(key)) {
                    try {
                        preloadImage(key)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to preload image for key: $key", e)
                    }
                }
            }
        }
        
        Log.d(TAG, "Image preloading completed")
    }
    
    /**
     * Clear all caches
     */
    fun clearCache() {
        Log.d(TAG, "Clearing image caches")
        imageCache.clear()
        drawableCache.clear()
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): Map<String, Int> {
        return mapOf(
            "imageCache" to imageCache.size,
            "drawableCache" to drawableCache.size
        )
    }
    
    /**
     * Create Coil ImageRequest for the resolved source
     */
    fun createImageRequest(result: ImageOverrideResult): ImageRequest {
        return ImageRequest.Builder(context)
            .data(result.source)
            .crossfade(true)
            .build()
    }
    
    /**
     * Load drawable from resolved source (for non-Compose usage)
     */
    suspend fun loadDrawable(result: ImageOverrideResult): Drawable? {
        val cacheKey = result.source.toString()
        
        // Check drawable cache
        drawableCache[cacheKey]?.let { cached ->
            Log.d(TAG, "Using cached drawable for source: ${result.source}")
            return cached
        }
        
        return try {
            val request = createImageRequest(result)
            val imageResult = imageLoader.execute(request)
            
            if (imageResult is SuccessResult) {
                val drawable = imageResult.drawable
                
                // Cache if enabled
                if (overrides.enableCaching) {
                    drawableCache[cacheKey] = drawable
                }
                
                Log.d(TAG, "Successfully loaded drawable from source: ${result.source}")
                drawable
            } else {
                Log.w(TAG, "Failed to load drawable from source: ${result.source}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading drawable from source: ${result.source}", e)
            null
        }
    }
    
    // === PRIVATE METHODS ===
    
    /**
     * Resolve the actual source from override string based on loading strategy
     */
    private fun resolveOverrideSource(overrideValue: String, strategy: ImageLoadingStrategy): ImageOverrideResult {
        val detectedStrategy = if (strategy == ImageLoadingStrategy.AUTO_DETECT) {
            detectLoadingStrategy(overrideValue)
        } else {
            strategy
        }
        
        val source = when (detectedStrategy) {
            ImageLoadingStrategy.URL -> {
                Log.d(TAG, "Resolving as URL: $overrideValue")
                overrideValue
            }
            
            ImageLoadingStrategy.FILE -> {
                Log.d(TAG, "Resolving as file path: $overrideValue")
                if (overrideValue.startsWith("file://")) {
                    Uri.parse(overrideValue)
                } else {
                    File(overrideValue)
                }
            }
            
            ImageLoadingStrategy.ASSET -> {
                Log.d(TAG, "Resolving as asset: $overrideValue")
                Uri.parse("file:///android_asset/$overrideValue")
            }
            
            ImageLoadingStrategy.RESOURCE -> {
                Log.d(TAG, "Resolving as resource ID: $overrideValue")
                try {
                    overrideValue.toInt()
                } catch (e: NumberFormatException) {
                    Log.w(TAG, "Invalid resource ID format: $overrideValue, treating as asset")
                    Uri.parse("file:///android_asset/$overrideValue")
                }
            }
            
            ImageLoadingStrategy.AUTO_DETECT -> {
                // This shouldn't happen, but fallback to asset
                Log.w(TAG, "AUTO_DETECT strategy not resolved, defaulting to asset")
                Uri.parse("file:///android_asset/$overrideValue")
            }
        }
        
        return ImageOverrideResult(
            source = source,
            strategy = detectedStrategy,
            isFallback = false
        )
    }
    
    /**
     * Auto-detect loading strategy based on override string format
     */
    private fun detectLoadingStrategy(overrideValue: String): ImageLoadingStrategy {
        return when {
            overrideValue.startsWith("http://") || overrideValue.startsWith("https://") -> {
                ImageLoadingStrategy.URL
            }
            
            overrideValue.startsWith("file://") || overrideValue.startsWith("/") -> {
                ImageLoadingStrategy.FILE
            }
            
            overrideValue.matches(Regex("^\\d+$")) -> {
                ImageLoadingStrategy.RESOURCE
            }
            
            else -> {
                ImageLoadingStrategy.ASSET
            }
        }
    }
    
    /**
     * Preload a single image
     */
    private suspend fun preloadImage(key: String) {
        val overrideValue = overrides.getOverride(key) ?: return
        val result = resolveOverrideSource(overrideValue, overrides.defaultLoadingStrategy)
        
        try {
            val request = createImageRequest(result)
            imageLoader.execute(request)
            Log.d(TAG, "Preloaded image for key: $key")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to preload image for key: $key", e)
        }
    }
}

/**
 * Extension function to easily check if an image has an override
 */
fun ImageOverrideManager.hasImageOverride(overrideKey: String): Boolean {
    return try {
        val instance = ImageOverrideManager.getInstance()
        // This is a simple check - in practice you'd access the overrides
        // through the manager's internal state
        true // Placeholder - would check actual overrides
    } catch (e: IllegalStateException) {
        false
    }
}
