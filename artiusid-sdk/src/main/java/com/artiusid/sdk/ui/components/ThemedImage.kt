/*
 * File: ThemedImage.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.components

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.artiusid.sdk.models.ImageLoadingStrategy
import com.artiusid.sdk.utils.ImageOverrideManager

/**
 * Themed Image component that supports override functionality
 * Automatically resolves between override sources and default SDK resources
 */
@Composable
fun ThemedImage(
    @DrawableRes defaultResourceId: Int,
    overrideKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    colorFilter: ColorFilter? = null,
    alpha: Float = 1.0f
) {
    val context = LocalContext.current
    
    // Try to get the override manager
    val overrideResult = remember(overrideKey, defaultResourceId) {
        try {
            if (ImageOverrideManager.isInitialized()) {
                val manager = ImageOverrideManager.getInstance()
                manager.resolveImageSource(defaultResourceId, overrideKey)
            } else {
                Log.w("ThemedImage", "ImageOverrideManager not initialized, using default resource")
                null
            }
        } catch (e: Exception) {
            Log.e("ThemedImage", "Error resolving image override for key: $overrideKey", e)
            null
        }
    }
    
    if (overrideResult != null && !overrideResult.isFallback && overrideResult.strategy != ImageLoadingStrategy.RESOURCE) {
        // Use override source with Coil
        Log.d("ThemedImage", "Using override for key '$overrideKey': ${overrideResult.source}")
        
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(overrideResult.source)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alignment = alignment,
            colorFilter = colorFilter,
            alpha = alpha,
            onError = { error ->
                Log.e("ThemedImage", "Failed to load override image for key '$overrideKey': ${error.result.throwable}")
            },
            onSuccess = {
                Log.d("ThemedImage", "Successfully loaded override image for key: $overrideKey")
            }
        )
    } else {
        // Use default SDK resource
        val resourceId = if (overrideResult?.strategy == ImageLoadingStrategy.RESOURCE) {
            overrideResult.source as? Int ?: defaultResourceId
        } else {
            defaultResourceId
        }
        
        Log.d("ThemedImage", "Using default resource for key '$overrideKey': $resourceId")
        
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alignment = alignment,
            colorFilter = colorFilter,
            alpha = alpha
        )
    }
}

/**
 * Themed GIF Animation component with override support
 * Specifically designed for animated GIF content
 */
@Composable
fun ThemedGifAnimation(
    @RawRes defaultResourceId: Int,
    overrideKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center
) {
    val context = LocalContext.current
    
    // Try to get the override manager
    val overrideResult = remember(overrideKey, defaultResourceId) {
        try {
            if (ImageOverrideManager.isInitialized()) {
                val manager = ImageOverrideManager.getInstance()
                manager.resolveGifSource(defaultResourceId, overrideKey)
            } else {
                Log.w("ThemedGifAnimation", "ImageOverrideManager not initialized, using default resource")
                null
            }
        } catch (e: Exception) {
            Log.e("ThemedGifAnimation", "Error resolving GIF override for key: $overrideKey", e)
            null
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = alignment
    ) {
        if (overrideResult != null && !overrideResult.isFallback && overrideResult.strategy != ImageLoadingStrategy.RESOURCE) {
            // Use override source with Coil
            Log.d("ThemedGifAnimation", "Using GIF override for key '$overrideKey': ${overrideResult.source}")
            
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(overrideResult.source)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onError = { error ->
                    Log.e("ThemedGifAnimation", "Failed to load override GIF for key '$overrideKey': ${error.result.throwable}")
                },
                onSuccess = {
                    Log.d("ThemedGifAnimation", "Successfully loaded override GIF for key: $overrideKey")
                }
            )
        } else {
            // Use default SDK resource
            val resourceId = if (overrideResult?.strategy == ImageLoadingStrategy.RESOURCE) {
                overrideResult.source as? Int ?: defaultResourceId
            } else {
                defaultResourceId
            }
            
            Log.d("ThemedGifAnimation", "Using default GIF resource for key '$overrideKey': $resourceId")
            
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resourceId)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onError = { error ->
                    Log.e("ThemedGifAnimation", "Failed to load default GIF resource $resourceId: ${error.result.throwable}")
                }
            )
        }
    }
}

/**
 * Get a Painter for themed images (for use in non-Compose contexts or special cases)
 */
@Composable
fun rememberThemedImagePainter(
    @DrawableRes defaultResourceId: Int,
    overrideKey: String
): Painter {
    val context = LocalContext.current
    
    val overrideResult = remember(overrideKey, defaultResourceId) {
        try {
            if (ImageOverrideManager.isInitialized()) {
                val manager = ImageOverrideManager.getInstance()
                manager.resolveImageSource(defaultResourceId, overrideKey)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("rememberThemedImagePainter", "Error resolving image override for key: $overrideKey", e)
            null
        }
    }
    
    return if (overrideResult != null && !overrideResult.isFallback && overrideResult.strategy != ImageLoadingStrategy.RESOURCE) {
        // Use override source
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(overrideResult.source)
                .crossfade(true)
                .build()
        )
    } else {
        // Use default resource
        val resourceId = if (overrideResult?.strategy == ImageLoadingStrategy.RESOURCE) {
            overrideResult.source as? Int ?: defaultResourceId
        } else {
            defaultResourceId
        }
        painterResource(id = resourceId)
    }
}

/**
 * Utility composable to check if an image has an override
 */
@Composable
fun rememberHasImageOverride(overrideKey: String): Boolean {
    return remember(overrideKey) {
        try {
            if (ImageOverrideManager.isInitialized()) {
                val manager = ImageOverrideManager.getInstance()
                // This would check the actual overrides in the manager
                // For now, return false as placeholder
                false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
