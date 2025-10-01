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
         * If already initialized, updates with new overrides
         */
        fun initialize(
            context: Context, 
            overrides: SDKImageOverrides,
            imageLoader: ImageLoader? = null
        ): ImageOverrideManager {
            return synchronized(this) {
                // Always create a new instance to ensure overrides are updated
                ImageOverrideManager(
                    context.applicationContext,
                    overrides,
                    imageLoader ?: ImageLoader(context)
                ).also { 
                    INSTANCE = it
                    val totalOverrides = countActiveOverrides(overrides)
                    Log.d(TAG, "ImageOverrideManager initialized with $totalOverrides active overrides (${overrides.customOverrides.size} custom)")
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
        
        /**
         * Count all active overrides (non-null fields + custom overrides)
         */
        private fun countActiveOverrides(overrides: SDKImageOverrides): Int {
            var count = 0
            
            
            // Count all non-null field overrides
            if (overrides.faceOverlay != null) count++
            if (overrides.faceUpGif != null) count++
            if (overrides.faceDownGif != null) count++
            if (overrides.phoneUpGif != null) count++
            if (overrides.phoneDownGif != null) count++
            if (overrides.faceRotationGif != null) count++
            if (overrides.passportOverlay != null) count++
            if (overrides.stateIdFrontOverlay != null) count++
            if (overrides.stateIdBackOverlay != null) count++
            if (overrides.passportAnimationGif != null) count++
            if (overrides.stateIdAnimationGif != null) count++
            if (overrides.backButtonIcon != null) count++
            if (overrides.cameraButtonIcon != null) count++
            if (overrides.doneIcon != null) count++
            if (overrides.documentRightArrow != null) count++
            if (overrides.scanFaceIcon != null) count++
            if (overrides.docScanIcon != null) count++
            if (overrides.passportIcon != null) count++
            if (overrides.stateIdIcon != null) count++
            if (overrides.focusIcon != null) count++
            if (overrides.successIcon != null) count++
            if (overrides.failedIcon != null) count++
            if (overrides.errorIcon != null) count++
            if (overrides.systemErrorIcon != null) count++
            if (overrides.approvalIcon != null) count++
            if (overrides.approvalRequestIcon != null) count++
            if (overrides.declinedIcon != null) count++
            if (overrides.informationalIcon != null) count++
            if (overrides.brandLogo != null) count++
            if (overrides.brandImage != null) count++
            if (overrides.introHomeImage != null) count++
            if (overrides.accountIcon != null) count++
            if (overrides.lockIcon != null) count++
            if (overrides.noGlassesIcon != null) count++
            if (overrides.noHatIcon != null) count++
            if (overrides.noMaskIcon != null) count++
            if (overrides.goodLightIcon != null) count++
            if (overrides.layFlatIcon != null) count++
            if (overrides.noGlareIcon != null) count++
            if (overrides.scanBackground01 != null) count++
            if (overrides.scanBackground02 != null) count++
            if (overrides.scanBackground03 != null) count++
            if (overrides.crossPlatformImage != null) count++
            if (overrides.crossDeviceImage != null) count++
            if (overrides.searchImage != null) count++
            if (overrides.groupImage != null) count++
            if (overrides.group254x353Image != null) count++
            if (overrides.group243Image != null) count++
            if (overrides.vector1Gray902 != null) count++
            if (overrides.vector1Gray903 != null) count++
            if (overrides.vector1Gray904 != null) count++
            if (overrides.vector1 != null) count++
            
            // Add custom overrides
            count += overrides.customOverrides.size
            
            return count
        }
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
     * Resolve the actual source from an override value based on the loading strategy
     */
    private fun resolveOverrideSource(overrideValue: String, strategy: ImageLoadingStrategy): ImageOverrideResult {
        Log.d(TAG, "Resolving override source: '$overrideValue' with strategy: $strategy")
        
        return when (strategy) {
            ImageLoadingStrategy.URL -> {
                Log.d(TAG, "Using URL strategy for: $overrideValue")
                ImageOverrideResult(
                    source = overrideValue,
                    strategy = ImageLoadingStrategy.URL,
                    isFallback = false
                )
            }
            ImageLoadingStrategy.ASSET -> {
                Log.d(TAG, "Using ASSET strategy for: $overrideValue")
                // For assets, we need to use the file:///android_asset/ prefix for Coil
                val assetPath = "file:///android_asset/$overrideValue"
                Log.d(TAG, "Asset path resolved to: $assetPath")
                ImageOverrideResult(
                    source = assetPath,
                    strategy = ImageLoadingStrategy.ASSET,
                    isFallback = false
                )
            }
            ImageLoadingStrategy.FILE -> {
                Log.d(TAG, "Using FILE strategy for: $overrideValue")
                ImageOverrideResult(
                    source = overrideValue,
                    strategy = ImageLoadingStrategy.FILE,
                    isFallback = false
                )
            }
            ImageLoadingStrategy.RESOURCE -> {
                Log.d(TAG, "Using RESOURCE strategy for: $overrideValue")
                val resourceId = overrideValue.toIntOrNull()
                if (resourceId != null) {
                    ImageOverrideResult(
                        source = resourceId,
                        strategy = ImageLoadingStrategy.RESOURCE,
                        isFallback = false
                    )
                } else {
                    Log.e(TAG, "Invalid resource ID: $overrideValue")
                    ImageOverrideResult(
                        source = overrideValue,
                        strategy = ImageLoadingStrategy.ASSET,
                        isFallback = true
                    )
                }
            }
            ImageLoadingStrategy.AUTO_DETECT -> {
                Log.d(TAG, "Using AUTO_DETECT strategy for: $overrideValue")
                when {
                    overrideValue.startsWith("http://") || overrideValue.startsWith("https://") -> {
                        Log.d(TAG, "Auto-detected URL: $overrideValue")
                        ImageOverrideResult(
                            source = overrideValue,
                            strategy = ImageLoadingStrategy.URL,
                            isFallback = false
                        )
                    }
                    overrideValue.startsWith("file://") -> {
                        Log.d(TAG, "Auto-detected FILE: $overrideValue")
                        ImageOverrideResult(
                            source = overrideValue,
                            strategy = ImageLoadingStrategy.FILE,
                            isFallback = false
                        )
                    }
                    overrideValue.matches(Regex("^\\d+$")) -> {
                        Log.d(TAG, "Auto-detected RESOURCE: $overrideValue")
                        val resourceId = overrideValue.toIntOrNull()
                        if (resourceId != null) {
                            ImageOverrideResult(
                                source = resourceId,
                                strategy = ImageLoadingStrategy.RESOURCE,
                                isFallback = false
                            )
                        } else {
                            Log.e(TAG, "Invalid resource ID in auto-detect: $overrideValue")
                            ImageOverrideResult(
                                source = "file:///android_asset/$overrideValue",
                                strategy = ImageLoadingStrategy.ASSET,
                                isFallback = true
                            )
                        }
                    }
                    else -> {
                        Log.d(TAG, "Auto-detected ASSET: $overrideValue")
                        ImageOverrideResult(
                            source = "file:///android_asset/$overrideValue",
                            strategy = ImageLoadingStrategy.ASSET,
                            isFallback = false
                        )
                    }
                }
            }
        }
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
