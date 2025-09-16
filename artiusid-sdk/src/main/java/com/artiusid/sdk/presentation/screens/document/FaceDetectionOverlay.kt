package com.artiusid.sdk.presentation.screens.document

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun FaceDetectionOverlay(
    faces: List<Rect>,
    modifier: Modifier = Modifier,
    isAligned: Boolean = true
) {
    val faceColor = if (isAligned) {
        Color(0xFF4CAF50) // Green for detected faces
    } else {
        Color(0xFFE53935) // Red for misaligned faces
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        faces.forEach { face ->
            drawRect(
                color = faceColor,
                topLeft = androidx.compose.ui.geometry.Offset(
                    face.left.toFloat(),
                    face.top.toFloat()
                ),
                size = androidx.compose.ui.geometry.Size(
                    face.width().toFloat(),
                    face.height().toFloat()
                ),
                style = Stroke(width = 2f)
            )
        }
    }
} 