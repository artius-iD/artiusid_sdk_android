/*
 * File: DocumentScanComponents.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.presentation.screens.document.DocumentType

@Composable
fun DocumentScanFrame(
    documentType: DocumentType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(if (documentType == DocumentType.PASSPORT) 1.4f else 1.6f)
            .padding(32.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cornerLength = width * 0.1f

            // Draw corner markers
            val path = Path().apply {
                // Top-left corner
                moveTo(0f, cornerLength)
                lineTo(0f, 0f)
                lineTo(cornerLength, 0f)

                // Top-right corner
                moveTo(width - cornerLength, 0f)
                lineTo(width, 0f)
                lineTo(width, cornerLength)

                // Bottom-right corner
                moveTo(width, height - cornerLength)
                lineTo(width, height)
                lineTo(width - cornerLength, height)

                // Bottom-left corner
                moveTo(cornerLength, height)
                lineTo(0f, height)
                lineTo(0f, height - cornerLength)
            }

            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 4f)
            )
        }

        // Instructions text
        Text(
            text = when (documentType) {
                DocumentType.ID_CARD -> "Position your ID card within the frame"
                DocumentType.PASSPORT -> "Position your passport within the frame"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
    }
}

@Composable
fun DocumentDetectionOverlay(
    documentType: DocumentType,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        // Clear area for document
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (documentType == DocumentType.PASSPORT) 1.4f else 1.6f)
                .align(Alignment.Center)
                .background(Color.Transparent)
        )

        // Edge detection guides
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2
            val guideLength = width * 0.15f

            // Draw edge detection guides
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(centerX - guideLength, centerY),
                end = Offset(centerX + guideLength, centerY),
                strokeWidth = 2f
            )

            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(centerX, centerY - guideLength),
                end = Offset(centerX, centerY + guideLength),
                strokeWidth = 2f
            )
        }

        // Status text
        Text(
            text = "Align document edges with the frame",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
} 