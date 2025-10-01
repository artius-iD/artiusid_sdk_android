/*
 * File: FaceDetectionOverlay.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

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
        com.artiusid.sdk.ui.theme.ThemedFaceDetection.getAlignedColor()
    } else {
        com.artiusid.sdk.ui.theme.ThemedFaceDetection.getMisalignedColor()
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