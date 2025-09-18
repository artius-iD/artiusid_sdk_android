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
    // Map direction to GIF resource and override key
    val (gifResourceId, overrideKey) = when (direction) {
        "Phone Up" -> R.raw.phone_up to "phone_up_gif"
        "Phone Down" -> R.raw.phone_down to "phone_down_gif"
        "Face Up" -> R.raw.face_up to "face_up_gif"
        "Face Down" -> R.raw.face_down to "face_down_gif"
        else -> null to null
    }
    
    android.util.Log.d("FacePositioningAnimationView", "🎬 Called with direction: '$direction'")
    android.util.Log.d("FacePositioningAnimationView", "🎬 Mapped to GIF resource: $gifResourceId, override key: $overrideKey")
    
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
 * Similar to iOS DirectionalIndicatorView
 */
@Composable
fun DirectionalIndicatorView(
    direction: String,
    modifier: Modifier = Modifier
) {
    // For now, show a simple text indicator
    // This can be enhanced with animated arrows later
    Box(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = direction,
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}
