package com.artiusid.sdk.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ui.theme.SDKColors

/**
 * Face scanning frame overlay component
 */
@Composable
fun FaceScanFrame(
    modifier: Modifier = Modifier,
    isDetecting: Boolean = false,
    quality: Float = 0f
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawFaceScanOverlay(
            isDetecting = isDetecting,
            quality = quality
        )
    }
}

/**
 * Draw face scan overlay
 */
private fun DrawScope.drawFaceScanOverlay(
    isDetecting: Boolean,
    quality: Float
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val ovalWidth = size.width * 0.6f
    val ovalHeight = size.height * 0.8f
    
    // Draw semi-transparent background
    drawRect(
        color = Color.Black.copy(alpha = 0.5f),
        size = size
    )
    
    // Create oval cutout for face
    val ovalRect = androidx.compose.ui.geometry.Rect(
        offset = Offset(
            centerX - ovalWidth / 2f,
            centerY - ovalHeight / 2f
        ),
        size = Size(ovalWidth, ovalHeight)
    )
    
    // Draw oval cutout (clear area)
    drawOval(
        color = Color.Transparent,
        topLeft = ovalRect.topLeft,
        size = ovalRect.size,
        blendMode = BlendMode.Clear
    )
    
    // Draw oval border
    val borderColor = when {
        quality > 0.7f -> SDKColors.Success
        quality > 0.4f -> SDKColors.Warning
        isDetecting -> SDKColors.Yellow900
        else -> SDKColors.WhiteA700
    }
    
    drawOval(
        color = borderColor,
        topLeft = ovalRect.topLeft,
        size = ovalRect.size,
        style = Stroke(width = 4.dp.toPx())
    )
    
    // Draw corner guides
    drawCornerGuides(ovalRect, borderColor)
}

/**
 * Draw corner guides for face positioning
 */
private fun DrawScope.drawCornerGuides(
    ovalRect: androidx.compose.ui.geometry.Rect,
    color: Color
) {
    val cornerLength = 30.dp.toPx()
    val strokeWidth = 3.dp.toPx()
    
    // Top-left corner
    drawLine(
        color = color,
        start = Offset(ovalRect.left, ovalRect.top + cornerLength),
        end = Offset(ovalRect.left, ovalRect.top),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(ovalRect.left, ovalRect.top),
        end = Offset(ovalRect.left + cornerLength, ovalRect.top),
        strokeWidth = strokeWidth
    )
    
    // Top-right corner
    drawLine(
        color = color,
        start = Offset(ovalRect.right - cornerLength, ovalRect.top),
        end = Offset(ovalRect.right, ovalRect.top),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(ovalRect.right, ovalRect.top),
        end = Offset(ovalRect.right, ovalRect.top + cornerLength),
        strokeWidth = strokeWidth
    )
    
    // Bottom-left corner
    drawLine(
        color = color,
        start = Offset(ovalRect.left, ovalRect.bottom - cornerLength),
        end = Offset(ovalRect.left, ovalRect.bottom),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(ovalRect.left, ovalRect.bottom),
        end = Offset(ovalRect.left + cornerLength, ovalRect.bottom),
        strokeWidth = strokeWidth
    )
    
    // Bottom-right corner
    drawLine(
        color = color,
        start = Offset(ovalRect.right - cornerLength, ovalRect.bottom),
        end = Offset(ovalRect.right, ovalRect.bottom),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(ovalRect.right, ovalRect.bottom - cornerLength),
        end = Offset(ovalRect.right, ovalRect.bottom),
        strokeWidth = strokeWidth
    )
}
