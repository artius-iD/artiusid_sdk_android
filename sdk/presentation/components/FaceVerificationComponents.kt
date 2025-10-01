/*
 * File: FaceVerificationComponents.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ui.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FaceScanFrame(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Pulsing animation
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Rotation animation
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        )
    )

    // Corner indicators animation
    val cornerScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(32.dp)
    ) {
        // Outer circle with rotation
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            rotate(rotation.value) {
                drawCircle(
                    color = Primary.copy(alpha = 0.3f),
                    style = Stroke(width = 4f)
                )
            }
        }

        // Animated inner circle
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            drawCircle(
                color = Primary,
                style = Stroke(width = 4f),
                radius = size.minDimension / 2 * scale.value
            )
        }

        // Face position indicators with animation
        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopStart)
                .padding(16.dp)
                .scale(cornerScale.value)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = if (isProcessing) Success else Primary,
                    style = Stroke(width = 2f)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .scale(cornerScale.value)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = if (isProcessing) Success else Primary,
                    style = Stroke(width = 2f)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .scale(cornerScale.value)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = if (isProcessing) Success else Primary,
                    style = Stroke(width = 2f)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .scale(cornerScale.value)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = if (isProcessing) Success else Primary,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FaceScanOverlay(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .alpha(animateFloatAsState(if (visible) 1f else 0f).value)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = isProcessing,
                transitionSpec = {
                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                }
            ) { processing ->
                if (processing) {
                    ProcessingIndicator()
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.animateContentSize()
                    ) {
                        Text(
                            text = "Position your face within the circle",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Make sure your face is well-lit and clearly visible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProcessingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Rotation animation
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )

    // Scale animation
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Success.copy(alpha = 0.2f))
            .scale(scale.value),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
        ) {
            rotate(rotation.value) {
                drawArc(
                    color = Success,
                    startAngle = 0f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}

@Composable
fun FaceVerificationResult(
    matchScore: Float,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Face Verification Complete",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Match score indicator
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    when {
                        matchScore >= 0.8f -> Success
                        matchScore >= 0.6f -> Warning
                        else -> Error
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(matchScore * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when {
                matchScore >= 0.8f -> "High Confidence Match"
                matchScore >= 0.6f -> "Moderate Confidence Match"
                else -> "Low Confidence Match"
            },
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecondaryButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            )

            CustomButton(
                text = "Continue",
                onClick = onContinue,
                modifier = Modifier.weight(1f),
                enabled = matchScore >= 0.7f
            )
        }
    }
} 