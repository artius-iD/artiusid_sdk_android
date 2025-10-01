/*
 * File: FaceIdAnimation.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ui.theme.Yellow900
import kotlin.math.*

/**
 * Face ID Animation Component
 * Inspired by https://dribbble.com/shots/6657160-face-ID-animation
 * Shows an animated face outline with scanning lines
 */
@Composable
fun FaceIdAnimation(
    modifier: Modifier = Modifier,
    isScanning: Boolean = false,
    scanColor: Color = Yellow900,
    faceColor: Color = Color.White.copy(alpha = 0.8f),
    size: Float = 120f
) {
    // Animation for the scanning lines
    val scanProgress by animateFloatAsState(
        targetValue = if (isScanning) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanProgress"
    )
    
    // Animation for the face outline
    val faceAlpha by animateFloatAsState(
        targetValue = if (isScanning) 1f else 0.6f,
        animationSpec = tween(500),
        label = "faceAlpha"
    )
    
    // Pulsing animation for when scanning
    val pulseScale by animateFloatAsState(
        targetValue = if (isScanning) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = if (isScanning) pulseScale else 1f
                    scaleY = if (isScanning) pulseScale else 1f
                }
        ) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val faceWidth = this.size.width * 0.6f
            val faceHeight = this.size.height * 0.8f
            
            // Draw face outline (rounded rectangle representing a face)
            drawFaceOutline(
                center = center,
                width = faceWidth,
                height = faceHeight,
                color = faceColor.copy(alpha = faceAlpha),
                strokeWidth = 3.dp.toPx()
            )
            
            // Draw face features (eyes, nose, mouth)
            drawFaceFeatures(
                center = center,
                faceWidth = faceWidth,
                faceHeight = faceHeight,
                color = faceColor.copy(alpha = faceAlpha),
                strokeWidth = 2.dp.toPx()
            )
            
            // Draw scanning lines when active
            if (isScanning) {
                drawScanningLines(
                    center = center,
                    faceWidth = faceWidth,
                    faceHeight = faceHeight,
                    progress = scanProgress,
                    color = scanColor,
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Draw corner brackets (Face ID style)
            drawCornerBrackets(
                center = center,
                width = faceWidth * 1.2f,
                height = faceHeight * 1.2f,
                color = if (isScanning) scanColor else faceColor.copy(alpha = faceAlpha),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawFaceOutline(
    center: Offset,
    width: Float,
    height: Float,
    color: Color,
    strokeWidth: Float
) {
    val rect = Rect(
        offset = Offset(center.x - width / 2, center.y - height / 2),
        size = androidx.compose.ui.geometry.Size(width, height)
    )
    
    drawRoundRect(
        color = color,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.2f),
        style = Stroke(width = strokeWidth)
    )
}

private fun DrawScope.drawFaceFeatures(
    center: Offset,
    faceWidth: Float,
    faceHeight: Float,
    color: Color,
    strokeWidth: Float
) {
    val eyeY = center.y - faceHeight * 0.15f
    val eyeRadius = faceWidth * 0.08f
    val eyeSpacing = faceWidth * 0.2f
    
    // Left eye
    drawCircle(
        color = color,
        radius = eyeRadius,
        center = Offset(center.x - eyeSpacing, eyeY),
        style = Stroke(width = strokeWidth)
    )
    
    // Right eye
    drawCircle(
        color = color,
        radius = eyeRadius,
        center = Offset(center.x + eyeSpacing, eyeY),
        style = Stroke(width = strokeWidth)
    )
    
    // Nose (simple line)
    val noseTop = Offset(center.x, center.y - faceHeight * 0.05f)
    val noseBottom = Offset(center.x, center.y + faceHeight * 0.05f)
    drawLine(
        color = color,
        start = noseTop,
        end = noseBottom,
        strokeWidth = strokeWidth
    )
    
    // Mouth (curved line)
    val mouthY = center.y + faceHeight * 0.2f
    val mouthWidth = faceWidth * 0.3f
    val path = Path().apply {
        moveTo(center.x - mouthWidth / 2, mouthY)
        quadraticBezierTo(
            center.x, mouthY + faceHeight * 0.08f,
            center.x + mouthWidth / 2, mouthY
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawScanningLines(
    center: Offset,
    faceWidth: Float,
    faceHeight: Float,
    progress: Float,
    color: Color,
    strokeWidth: Float
) {
    val numberOfLines = 8
    val lineSpacing = faceHeight / numberOfLines
    val startY = center.y - faceHeight / 2
    
    for (i in 0 until numberOfLines) {
        val lineY = startY + (lineSpacing * i)
        val lineProgress = ((progress * numberOfLines) - i).coerceIn(0f, 1f)
        
        if (lineProgress > 0f) {
            val alpha = sin(lineProgress * PI).toFloat()
            val currentColor = color.copy(alpha = alpha * 0.8f)
            
            // Create gradient effect
            val brush = Brush.horizontalGradient(
                colors = listOf(
                    currentColor.copy(alpha = 0f),
                    currentColor,
                    currentColor.copy(alpha = 0f)
                ),
                startX = center.x - faceWidth / 2,
                endX = center.x + faceWidth / 2
            )
            
            drawLine(
                brush = brush,
                start = Offset(center.x - faceWidth / 2, lineY),
                end = Offset(center.x + faceWidth / 2, lineY),
                strokeWidth = strokeWidth * 1.5f
            )
        }
    }
}

private fun DrawScope.drawCornerBrackets(
    center: Offset,
    width: Float,
    height: Float,
    color: Color,
    strokeWidth: Float
) {
    val bracketLength = min(width, height) * 0.15f
    val rect = Rect(
        offset = Offset(center.x - width / 2, center.y - height / 2),
        size = androidx.compose.ui.geometry.Size(width, height)
    )
    
    // Top-left bracket
    drawLine(
        color = color,
        start = Offset(rect.left, rect.top + bracketLength),
        end = Offset(rect.left, rect.top),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(rect.left, rect.top),
        end = Offset(rect.left + bracketLength, rect.top),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Top-right bracket
    drawLine(
        color = color,
        start = Offset(rect.right - bracketLength, rect.top),
        end = Offset(rect.right, rect.top),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(rect.right, rect.top),
        end = Offset(rect.right, rect.top + bracketLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Bottom-left bracket
    drawLine(
        color = color,
        start = Offset(rect.left, rect.bottom - bracketLength),
        end = Offset(rect.left, rect.bottom),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(rect.left, rect.bottom),
        end = Offset(rect.left + bracketLength, rect.bottom),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Bottom-right bracket
    drawLine(
        color = color,
        start = Offset(rect.right - bracketLength, rect.bottom),
        end = Offset(rect.right, rect.bottom),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(rect.right, rect.bottom - bracketLength),
        end = Offset(rect.right, rect.bottom),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}