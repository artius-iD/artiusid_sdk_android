/*
 * File: DocumentOverlay.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ui.theme.AppDocumentColors
import com.artiusid.sdk.ui.theme.AppOverlayColors

@Composable
fun DocumentOverlay(
    modifier: Modifier = Modifier,
    documentBounds: android.graphics.RectF? = null,
    isAligned: Boolean = true
) {
    val rectColor = if (isAligned) {
        AppDocumentColors.aligned
    } else {
        AppDocumentColors.misaligned
    }
    
    val overlayColor = AppOverlayColors.overlay
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw semi-transparent overlay
            drawRect(
                color = overlayColor,
                size = size
            )

            // Draw document rectangle if bounds are available
            documentBounds?.let { bounds ->
                drawRect(
                    color = rectColor,
                    style = Stroke(width = 4.dp.toPx()),
                    topLeft = Offset(bounds.left, bounds.top),
                    size = Size(bounds.width(), bounds.height())
                )
            }
        }
    }
} 