package com.artiusid.sdk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ui.components.GradientBackground
import com.artiusid.sdk.ui.theme.SDKColors
import com.artiusid.sdk.models.*

/**
 * Simplified screens for SDK navigation
 * These provide the basic functionality while we fix the detailed screens
 */

@Composable
fun FaceScanIntroScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Face Liveness Check",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SDKColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "We'll verify that you're a real person",
                fontSize = 16.sp,
                color = SDKColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
fun FaceScanScreen(
    onFaceScanComplete: (LivenessResult) -> Unit,
    onBack: () -> Unit,
    onError: (String) -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Face Scan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Simulate face scan
            Button(
                onClick = {
                    onFaceScanComplete(
                        LivenessResult(
                            success = true,
                            isLive = true,
                            confidence = 0.95f,
                            faceBitmap = null,
                            livenessScore = 0.92f,
                            processingTime = 2000L,
                            sessionId = "face_${System.currentTimeMillis()}"
                        )
                    )
                }
            ) {
                Text("Simulate Face Scan")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
fun SelectDocumentTypeScreen(
    onDocumentTypeSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Select Document Type",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onDocumentTypeSelected("id") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Driver's License / ID Card")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onDocumentTypeSelected("passport") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Passport")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

// Additional simplified screens for complete navigation

@Composable
fun DocumentScanBackIntroScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    SimpleIntroScreen(
        title = "Scan Back of Document",
        description = "Now scan the back of your document",
        onContinue = onContinue,
        onBack = onBack
    )
}

@Composable
fun DocumentScanBackScreen(
    onDocumentScanned: (DocumentScanResult) -> Unit,
    onBack: () -> Unit,
    onError: (String) -> Unit
) {
    DocumentScanScreen(
        documentType = "back",
        onDocumentScanned = onDocumentScanned,
        onBack = onBack,
        onError = onError
    )
}

@Composable
fun PassportScanIntroScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    SimpleIntroScreen(
        title = "Passport Scan",
        description = "Scan your passport's photo page",
        onContinue = onContinue,
        onBack = onBack
    )
}

@Composable
fun PassportScanScreen(
    onPassportScanned: (DocumentScanResult) -> Unit,
    onBack: () -> Unit,
    onError: (String) -> Unit
) {
    DocumentScanScreen(
        documentType = "passport",
        onDocumentScanned = onPassportScanned,
        onBack = onBack,
        onError = onError
    )
}

@Composable
fun PassportChipIntroScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NFC Chip Reading",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Read your passport's NFC chip for enhanced security",
                fontSize = 16.sp,
                color = SDKColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onSkip) {
                Text("Skip")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
fun PassportChipScanScreen(
    onNfcComplete: (NFCPassportResult) -> Unit,
    onBack: () -> Unit,
    onError: (String) -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NFC Reading",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    onNfcComplete(
                        NFCPassportResult(
                            nfcData = null, // TODO: Create proper PassportNFCData
                            success = true,
                            isAuthenticated = true,
                            expiresAt = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L), // 1 year from now
                            processingTime = 3000L,
                            sessionId = "nfc_${System.currentTimeMillis()}"
                        )
                    )
                }
            ) {
                Text("Simulate NFC Reading")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
fun VerificationProcessingScreen(
    onProcessingComplete: (VerificationResult) -> Unit,
    onError: (String) -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Processing Verification",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            CircularProgressIndicator()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Please wait while we verify your identity...")
            
            // Simulate processing
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                onProcessingComplete(
                    VerificationResult(
                        success = true,
                        livenessResult = null,
                        documentResult = null,
                        nfcResult = null
                    )
                )
            }
        }
    }
}

@Composable
fun VerificationResultsScreen(
    onComplete: (VerificationResult) -> Unit,
    onRetry: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✅ Verification Complete",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SDKColors.Success
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    onComplete(
                        VerificationResult(
                            success = true,
                            livenessResult = null,
                            documentResult = null,
                            nfcResult = null
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Complete")
            }
        }
    }
}

@Composable
fun VerificationFailureScreen(
    failureType: String,
    errorReason: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "❌ Verification Failed",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SDKColors.Error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = errorReason,
                fontSize = 16.sp,
                color = SDKColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Try Again")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun AuthenticationScreen(
    onAuthenticationComplete: (AuthenticationResult) -> Unit,
    onBack: () -> Unit,
    onError: (String) -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Authentication",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    onAuthenticationComplete(
                        AuthenticationResult(
                            isAuthenticated = true,
                            userId = "auth_user_${System.currentTimeMillis()}",
                            token = "auth_token_${System.currentTimeMillis()}"
                        )
                    )
                }
            ) {
                Text("Simulate Authentication")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
fun AuthenticatedScreen(
    onComplete: (AuthenticationResult) -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✅ Authentication Complete",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SDKColors.Success
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    onComplete(
                        AuthenticationResult(
                            isAuthenticated = true,
                            userId = "final_auth_user_${System.currentTimeMillis()}",
                            token = "final_auth_token_${System.currentTimeMillis()}"
                        )
                    )
                }
            ) {
                Text("Complete")
            }
        }
    }
}
@Composable
fun DocumentScanIntroScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    SimpleIntroScreen(
        title = "Document Scan",
        description = "Scan the front of your document",
        onContinue = onContinue,
        onBack = onBack
    )
}

@Composable
fun DocumentScanScreen(
    documentType: String,
    onDocumentScanned: (DocumentScanResult) -> Unit,
    onBack: () -> Unit,
    onError: (String) -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Scan $documentType",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    onDocumentScanned(
                        DocumentScanResult(
                            success = true,
                            documentType = when(documentType) {
                                "passport" -> "PASSPORT"
                                "id" -> "ID_CARD"
                                "back" -> "ID_CARD"
                                else -> "OTHER"
                            },
                            frontImage = null,
                            backImage = null,
                            extractedData = mapOf("name" to "John Doe"),
                            confidence = 0.88f,
                            processingTime = 1500L,
                            sessionId = "doc_${System.currentTimeMillis()}"
                        )
                    )
                }
            ) {
                Text("Simulate Document Scan")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

// Helper composable for simple intro screens
@Composable
private fun SimpleIntroScreen(
    title: String,
    description: String,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SDKColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = description,
                fontSize = 16.sp,
                color = SDKColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
