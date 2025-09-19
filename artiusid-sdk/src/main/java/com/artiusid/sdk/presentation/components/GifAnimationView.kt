/*
 * File: GifAnimationView.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.artiusid.sdk.R
import com.artiusid.sdk.ui.components.ThemedGifAnimation

@Composable
fun GifAnimationView(
    gifResourceId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String = "Animation"
) {
    val context = LocalContext.current
    
    android.util.Log.e("GifAnimationView", "🚨🚨🚨 GIFANIMATIONVIEW CALLED WITH RESOURCE: $gifResourceId 🚨🚨🚨")
    android.util.Log.d("GifAnimationView", "🎬 Loading GIF resource: $gifResourceId")
    
    Box(
        modifier = modifier.background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(gifResourceId)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onSuccess = { 
                android.util.Log.d("GifAnimationView", "🎬 ✅ GIF loaded successfully: $gifResourceId")
            },
            onError = { error ->
                android.util.Log.e("GifAnimationView", "🎬 ❌ GIF failed to load: $gifResourceId, error: ${error.result.throwable}")
            }
        )
    }
}

@Composable
fun FacePositioningAnimationView(
    direction: String,
    modifier: Modifier = Modifier
) {
    // Enhanced direction mapping with better fallbacks
    // TEMPORARILY force Face Left/Right to use arrows for testing
    val (gifResourceId, overrideKey) = when (direction) {
        "Phone Up" -> R.raw.phone_up to "phone_up_gif"
        "Phone Down" -> R.raw.phone_down to "phone_down_gif"
        "Face Up" -> R.raw.face_up to "face_up_gif"
        "Face Down" -> R.raw.face_down to "face_down_gif"
        // FORCE these to use arrows instead of GIFs for testing
        "Face Left", "Face Right" -> null to null
        // Map diagonal directions to closest available GIF
        "Face Down-Left", "Face Down-Right" -> R.raw.face_down to "face_down_gif"
        "Face Up-Left", "Face Up-Right" -> R.raw.face_up to "face_up_gif"
        else -> null to null
    }
    
    android.util.Log.e("FacePositioningAnimationView", "🎬🎬🎬 CALLED WITH DIRECTION: '$direction' 🎬🎬🎬")
    android.util.Log.e("FacePositioningAnimationView", "🎬🎬🎬 MAPPED TO GIF: $gifResourceId, OVERRIDE: $overrideKey 🎬🎬🎬")
    
    if (gifResourceId != null && overrideKey != null) {
        android.util.Log.d("FacePositioningAnimationView", "🎬 Showing ThemedGifAnimation for: $direction")
        ThemedGifAnimation(
            defaultResourceId = gifResourceId,
            overrideKey = overrideKey,
            contentDescription = "Face positioning guidance: $direction",
            modifier = modifier
        )
    } else if (direction.isNotEmpty()) {
        android.util.Log.d("FacePositioningAnimationView", "🎬 No GIF resource - showing DirectionalIndicatorView for: $direction")
        // For directions without GIF assets (Face Left, Face Right, etc.)
        // Show a directional indicator similar to iOS DirectionalIndicatorView
        DirectionalIndicatorView(
            direction = direction,
            modifier = modifier
        )
    } else {
        android.util.Log.d("FacePositioningAnimationView", "🎬 Empty direction - showing nothing")
    }
}

/**
 * Directional indicator for face positioning directions that don't have GIF assets
 * Uses clean arrow assets similar to iOS ARKit DirectionalIndicatorView
 */
@Composable
fun DirectionalIndicatorView(
    direction: String,
    modifier: Modifier = Modifier
) {
    android.util.Log.e("DirectionalIndicatorView", "🎯🎯🎯 DIRECTIONAL INDICATOR CALLED FOR: $direction 🎯🎯🎯")
    
    // Map direction to arrow resource
    val arrowResource = when (direction) {
        "Face Left" -> R.drawable.arrow_left
        "Face Right" -> R.drawable.arrow_right
        "Face Up", "Face Up-Left", "Face Up-Right" -> R.drawable.arrow_up
        "Face Down", "Face Down-Left", "Face Down-Right" -> R.drawable.arrow_down
        else -> R.drawable.arrow_up // Default fallback
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Show a bright background for debugging
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.Red.copy(alpha = 0.7f), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = arrowResource),
                contentDescription = "Direction: $direction",
                modifier = Modifier.size(64.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
            )
        }
    }
}

