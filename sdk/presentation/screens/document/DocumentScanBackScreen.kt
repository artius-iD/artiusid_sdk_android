/*
 * File: DocumentScanBackScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.utils.DocumentSide
import com.artiusid.sdk.presentation.components.DocumentCameraPreview

@Composable
fun DocumentScanBackScreen(
    onDocumentScanComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DocumentScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Set document side to BACK for barcode detection
    LaunchedEffect(Unit) {
        viewModel.setDocumentSide(DocumentSide.BACK)
        Log.d("DocumentScanBackScreen", "Set document side to BACK for barcode detection")
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Use our DocumentCameraPreview with barcode detection pipeline
            DocumentCameraPreview(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )
            
            // Document overlay
            DocumentOverlay(
                modifier = Modifier.fillMaxSize()
            )
            
            // Instructions
            InstructionsOverlay(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
            
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor()
                )
            }
        } else {
            PermissionRequest(
                onRequestPermission = {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            )
        }
    }
}

// CameraPreview function removed - using DocumentCameraPreview with barcode detection pipeline

@Composable
private fun DocumentOverlay(
    modifier: Modifier = Modifier
) {
    val overlayColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val rectWidth = size.width * 0.8f
        val rectHeight = rectWidth * 0.6f
        
        // Draw document frame
        val documentPath = Path().apply {
            moveTo(centerX - rectWidth / 2, centerY - rectHeight / 2)
            lineTo(centerX + rectWidth / 2, centerY - rectHeight / 2)
            lineTo(centerX + rectWidth / 2, centerY + rectHeight / 2)
            lineTo(centerX - rectWidth / 2, centerY + rectHeight / 2)
            close()
        }
        
        drawPath(
            path = documentPath,
            color = overlayColor,
            style = Stroke(width = 4f)
        )
        
        // Draw corner indicators
        val cornerSize = 40f
        val cornerStroke = 6f
        
        // Top-left corner
        drawLine(
            color = overlayColor,
            start = Offset(centerX - rectWidth / 2, centerY - rectHeight / 2 + cornerSize),
            end = Offset(centerX - rectWidth / 2, centerY - rectHeight / 2),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = overlayColor,
            start = Offset(centerX - rectWidth / 2, centerY - rectHeight / 2),
            end = Offset(centerX - rectWidth / 2 + cornerSize, centerY - rectHeight / 2),
            strokeWidth = cornerStroke
        )
        
        // Top-right corner
        drawLine(
            color = overlayColor,
            start = Offset(centerX + rectWidth / 2 - cornerSize, centerY - rectHeight / 2),
            end = Offset(centerX + rectWidth / 2, centerY - rectHeight / 2),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = overlayColor,
            start = Offset(centerX + rectWidth / 2, centerY - rectHeight / 2),
            end = Offset(centerX + rectWidth / 2, centerY - rectHeight / 2 + cornerSize),
            strokeWidth = cornerStroke
        )
        
        // Bottom-left corner
        drawLine(
            color = overlayColor,
            start = Offset(centerX - rectWidth / 2, centerY + rectHeight / 2 - cornerSize),
            end = Offset(centerX - rectWidth / 2, centerY + rectHeight / 2),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = overlayColor,
            start = Offset(centerX - rectWidth / 2, centerY + rectHeight / 2),
            end = Offset(centerX - rectWidth / 2 + cornerSize, centerY + rectHeight / 2),
            strokeWidth = cornerStroke
        )
        
        // Bottom-right corner
        drawLine(
            color = overlayColor,
            start = Offset(centerX + rectWidth / 2 - cornerSize, centerY + rectHeight / 2),
            end = Offset(centerX + rectWidth / 2, centerY + rectHeight / 2),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = overlayColor,
            start = Offset(centerX + rectWidth / 2, centerY + rectHeight / 2),
            end = Offset(centerX + rectWidth / 2, centerY + rectHeight / 2 - cornerSize),
            strokeWidth = cornerStroke
        )
    }
}

@Composable
private fun InstructionsOverlay(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Text(
            text = "ALIGN ID BACK IN FRAME AS SHOWN",
            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun PermissionRequest(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This app needs camera access to scan your document",
            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor().copy(alpha = 0.8f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
            )
        ) {
            Text("Grant Permission")
        }
    }
} 