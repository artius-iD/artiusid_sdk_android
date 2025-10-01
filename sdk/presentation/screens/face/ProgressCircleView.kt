/*
 * Author: Todd Bryant
 * Company: artius.iD
 * Enhanced ProgressCircleView with iOS-like segmented circle animation
 */

package com.artiusid.sdk.presentation.screens.face

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ui.theme.AppColors
import com.artiusid.sdk.ui.theme.AppFaceColors
import kotlin.math.*
import kotlinx.coroutines.delay

@Composable
fun ProgressCircleView(
    segmentStatus: List<Boolean>,
    modifier: Modifier = Modifier
) {
    // Animation state
    var animationRotation by remember { mutableStateOf(0f) }
    val completedCount = segmentStatus.count { it }
    val isCompleted = completedCount == 8
    
    // Get colors outside Canvas scope
    val segmentCompleteColor = AppFaceColors.segmentComplete
    val segmentIncompleteColor = AppFaceColors.segmentIncomplete
    val textPrimaryColor = AppColors.textPrimary
    val textSecondaryColor = AppColors.textSecondary
    
    // Animate rotation for completion indicator
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            while (true) {
                animationRotation += 360f
                delay(1000)
            }
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = kotlin.math.min(size.width, size.height) / 2
            val segmentCircleRadius = outerRadius * 0.95f  // Position segments closer to the edge of the camera circle
            val strokeWidth = segmentCircleRadius * 0.08f  // Slightly thinner stroke for better proportion
            
            // Background circle segments (iOS-like background)
            val segmentAngle = 360f / 8f
            val segmentLength = 0.8f // 80% of segment length with gap
            
            for (i in 0 until 8) {
                val startAngle = i * segmentAngle - 90f - 22.5f // Start from top, iOS rotation offset
                val sweepAngle = segmentAngle * segmentLength
                
                // Background segment - removed to eliminate unwanted grey circle
                
                // Progress segment with enhanced colors and animation (iOS-like)
                val segmentColor = if (segmentStatus.getOrNull(i) == true) {
                    segmentCompleteColor
                } else {
                    segmentIncompleteColor.copy(alpha = 0.6f)
                }
                val isCurrentSegment = segmentStatus.getOrNull(i) == true
                val scale = if (isCurrentSegment) 1.1f else 1.0f
                
                drawArc(
                    color = segmentColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth * scale,
                        cap = StrokeCap.Round
                    ),
                    size = Size(segmentCircleRadius * 2 * scale, segmentCircleRadius * 2 * scale),
                    topLeft = Offset(
                        center.x - segmentCircleRadius * scale, 
                        center.y - segmentCircleRadius * scale
                    )
                )
            }
        }
        
        // Completion indicator (iOS-like checkmark animation)
        if (isCompleted) {
            Canvas(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        rotationZ = animationRotation
                        scaleX = 1.0f + 0.1f * sin(animationRotation * PI.toFloat() / 180f)
                        scaleY = 1.0f + 0.1f * sin(animationRotation * PI.toFloat() / 180f)
                    }
            ) {
                val checkmarkSize = size.minDimension * 0.6f
                val strokeWidth = checkmarkSize * 0.1f
                val center = Offset(size.width / 2, size.height / 2)
                
                // Draw checkmark
                val path = Path().apply {
                    moveTo(center.x - checkmarkSize * 0.25f, center.y)
                    lineTo(center.x - checkmarkSize * 0.1f, center.y + checkmarkSize * 0.15f)
                    lineTo(center.x + checkmarkSize * 0.25f, center.y - checkmarkSize * 0.15f)
                }
                
                drawPath(
                    path = path,
                    color = Color.Green,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
        
        // Progress text with completion count (iOS-like)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = 120.dp) // Position below the circle
        ) {
            Text(
                text = "$completedCount/8 segments",
                color = textPrimaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Removed "Now blink!" text - only show head turning instruction
            Text(
                text = "Turn your head slowly",
                color = textSecondaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

 