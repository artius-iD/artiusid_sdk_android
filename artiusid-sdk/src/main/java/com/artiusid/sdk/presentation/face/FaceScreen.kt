/*
 * File: FaceScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.face

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artiusid.sdk.services.FaceMeshDetectorService
import com.artiusid.sdk.services.FaceMeshDetectorServiceImpl

@Composable
fun FaceScreen(
    onNavigateToVerification: () -> Unit
) {
    val context = LocalContext.current
    val faceMeshDetectorService: FaceMeshDetectorService = remember { FaceMeshDetectorServiceImpl(context) }
    val viewModel: FaceViewModel = remember { FaceViewModel(faceMeshDetectorService) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.startFaceDetection()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            
            // Status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Face Detection",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Face detection status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.hasFace) Color.Green else Color.Red
                                )
                        )
                        
                        Text(
                            text = if (uiState.hasFace) "Face Detected" else "No Face Detected",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    if (uiState.hasFace) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confidence: ${(uiState.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error: ${uiState.error}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red
                        )
                    }
                    
                    if (uiState.isProcessing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Continue button
            Button(
                onClick = onNavigateToVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = uiState.hasFace && !uiState.isProcessing
            ) {
                Text(
                    text = "Continue to Verification",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
} 