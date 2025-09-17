/*
 * Author: Todd Bryant
 * Company: artius.iD
 * GIF Animation Component for Face Positioning Guidance
 */

package com.artiusid.sdk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@Composable
fun GifAnimationView(
    gifResourceId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String = "Animation"
) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(gifResourceId)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier.background(Color.Transparent)
    )
}

@Composable
fun FacePositioningAnimationView(
    direction: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Map direction to GIF resource
    val gifResourceId = when (direction) {
        "Phone Up" -> com.artiusid.sdk.R.raw.phone_up
        "Phone Down" -> com.artiusid.sdk.R.raw.phone_down
        "Face Up" -> com.artiusid.sdk.R.raw.face_up
        "Face Down" -> com.artiusid.sdk.R.raw.face_down
        else -> null
    }
    
    if (gifResourceId != null) {
        GifAnimationView(
            gifResourceId = gifResourceId,
            modifier = modifier,
            contentDescription = "Face positioning guidance: $direction"
        )
    } else if (direction.isNotEmpty()) {
        // For directions without GIF assets (Face Left, Face Right, etc.)
        // Show a directional indicator similar to iOS DirectionalIndicatorView
        DirectionalIndicatorView(
            direction = direction,
            modifier = modifier
        )
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
