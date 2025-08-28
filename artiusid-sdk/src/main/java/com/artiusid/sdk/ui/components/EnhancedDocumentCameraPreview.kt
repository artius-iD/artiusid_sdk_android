package com.artiusid.sdk.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ui.theme.SDKColors

/**
 * Enhanced camera preview component for document scanning
 */
@Composable
fun EnhancedDocumentCameraPreview(
    modifier: Modifier = Modifier,
    isDetecting: Boolean = false,
    quality: Float = 0f,
    documentType: String = "ID"
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview would go here in real implementation
        // For now, we'll show the overlay
        
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawDocumentScanOverlay(
                isDetecting = isDetecting,
                quality = quality,
                documentType = documentType
            )
        }
    }
}

/**
 * Draw document scan overlay
 */
private fun DrawScope.drawDocumentScanOverlay(
    isDetecting: Boolean,
    quality: Float,
    documentType: String
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    
    // Document frame dimensions based on type
    val (frameWidth, frameHeight) = when (documentType.lowercase()) {
        "passport" -> Pair(size.width * 0.8f, size.width * 0.8f * 0.7f) // Passport ratio
        else -> Pair(size.width * 0.85f, size.width * 0.85f * 0.63f) // ID card ratio
    }
    
    // Draw semi-transparent background
    drawRect(
        color = Color.Black.copy(alpha = 0.6f),
        size = size
    )
    
    // Create document frame cutout
    val frameRect = androidx.compose.ui.geometry.Rect(
        offset = Offset(
            centerX - frameWidth / 2f,
            centerY - frameHeight / 2f
        ),
        size = Size(frameWidth, frameHeight)
    )
    
    // Draw frame cutout (clear area)
    drawRoundRect(
        color = Color.Transparent,
        topLeft = frameRect.topLeft,
        size = frameRect.size,
        cornerRadius = CornerRadius(12.dp.toPx()),
        blendMode = BlendMode.Clear
    )
    
    // Draw frame border
    val borderColor = when {
        quality > 0.7f -> SDKColors.Success
        quality > 0.4f -> SDKColors.Warning
        isDetecting -> SDKColors.Yellow900
        else -> SDKColors.WhiteA700
    }
    
    drawRoundRect(
        color = borderColor,
        topLeft = frameRect.topLeft,
        size = frameRect.size,
        cornerRadius = CornerRadius(12.dp.toPx()),
        style = Stroke(width = 3.dp.toPx())
    )
    
    // Draw corner guides
    drawDocumentCornerGuides(frameRect, borderColor)
}

/**
 * Draw corner guides for document positioning
 */
private fun DrawScope.drawDocumentCornerGuides(
    frameRect: androidx.compose.ui.geometry.Rect,
    color: Color
) {
    val cornerLength = 25.dp.toPx()
    val strokeWidth = 4.dp.toPx()
    val cornerOffset = 8.dp.toPx()
    
    // Top-left corner
    drawLine(
        color = color,
        start = Offset(frameRect.left - cornerOffset, frameRect.top - cornerOffset + cornerLength),
        end = Offset(frameRect.left - cornerOffset, frameRect.top - cornerOffset),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(frameRect.left - cornerOffset, frameRect.top - cornerOffset),
        end = Offset(frameRect.left - cornerOffset + cornerLength, frameRect.top - cornerOffset),
        strokeWidth = strokeWidth
    )
    
    // Top-right corner
    drawLine(
        color = color,
        start = Offset(frameRect.right + cornerOffset - cornerLength, frameRect.top - cornerOffset),
        end = Offset(frameRect.right + cornerOffset, frameRect.top - cornerOffset),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(frameRect.right + cornerOffset, frameRect.top - cornerOffset),
        end = Offset(frameRect.right + cornerOffset, frameRect.top - cornerOffset + cornerLength),
        strokeWidth = strokeWidth
    )
    
    // Bottom-left corner
    drawLine(
        color = color,
        start = Offset(frameRect.left - cornerOffset, frameRect.bottom + cornerOffset - cornerLength),
        end = Offset(frameRect.left - cornerOffset, frameRect.bottom + cornerOffset),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(frameRect.left - cornerOffset, frameRect.bottom + cornerOffset),
        end = Offset(frameRect.left - cornerOffset + cornerLength, frameRect.bottom + cornerOffset),
        strokeWidth = strokeWidth
    )
    
    // Bottom-right corner
    drawLine(
        color = color,
        start = Offset(frameRect.right + cornerOffset - cornerLength, frameRect.bottom + cornerOffset),
        end = Offset(frameRect.right + cornerOffset, frameRect.bottom + cornerOffset),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(frameRect.right + cornerOffset, frameRect.bottom + cornerOffset - cornerLength),
        end = Offset(frameRect.right + cornerOffset, frameRect.bottom + cornerOffset),
        strokeWidth = strokeWidth
    )
}
