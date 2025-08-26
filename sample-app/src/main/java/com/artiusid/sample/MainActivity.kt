package com.artiusid.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.callbacks.*
import com.artiusid.sdk.config.*
import com.artiusid.sdk.models.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK with comprehensive configuration
        initializeSDK()
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
    
    private fun initializeSDK() {
        val config = ArtiusSDKConfig.Builder()
            .setApiKey("sample-api-key")
            .setEnvironment(Environment.DEVELOPMENT)
            .setBrandingConfig(
                BrandingConfig(
                    colorScheme = ColorScheme.LIGHT,
                    companyName = "Sample Company",
                    companyLogo = "sample_logo"
                )
            )
            .setLocalizationConfig(
                LocalizationConfig(
                    defaultLanguage = "en",
                    supportedLanguages = listOf("en", "es", "fr")
                )
            )
            .setVerificationConfig(
                VerificationConfig(
                    enableFaceVerification = true,
                    enableDocumentScanning = true,
                    enableNFCReading = false
                )
            )
            .setDebugMode(true)
            .build()
            
        ArtiusIDSDK.initialize(this, config)
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var resultText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "ArtiusID SDK Sample",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Demonstrate SDK capabilities",
            style = MaterialTheme.typography.bodyLarge
        )
        
        if (resultText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = resultText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Complete Verification Flow
        Button(
            onClick = {
                ArtiusIDSDK.startVerificationFlow(
                    activity = context as ComponentActivity,
                    callback = object : VerificationCallback {
                        override fun onSuccess(result: VerificationResult) {
                            resultText = "Verification successful! Session: ${result.sessionId}"
                        }
                        
                        override fun onError(error: SDKError) {
                            resultText = "Verification failed: ${error.message}"
                        }
                        
                        override fun onCancelled() {
                            resultText = "Verification cancelled by user"
                        }
                        
                        override fun onProgress(step: VerificationStep, progress: Int) {
                            resultText = "Progress: $step - $progress%"
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Verification Flow")
        }
        
        // Face Liveness Only
        Button(
            onClick = {
                ArtiusIDSDK.startFaceLiveness(
                    activity = context as ComponentActivity,
                    callback = object : LivenessCallback {
                        override fun onSuccess(result: LivenessResult) {
                            resultText = "Face liveness successful! Score: ${result.livenessScore}"
                        }
                        
                        override fun onError(error: SDKError) {
                            resultText = "Face liveness failed: ${error.message}"
                        }
                        
                        override fun onCancelled() {
                            resultText = "Face liveness cancelled"
                        }
                        
                        override fun onProgress(segmentsCompleted: Int, totalSegments: Int, currentInstruction: String) {
                            resultText = "Progress: $segmentsCompleted/$totalSegments - $currentInstruction"
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Face Liveness Detection")
        }
        
        // Document Scan Only
        Button(
            onClick = {
                ArtiusIDSDK.startDocumentScan(
                    activity = context as ComponentActivity,
                    documentType = DocumentType.ID_CARD,
                    callback = object : DocumentScanCallback {
                        override fun onSuccess(result: DocumentScanResult) {
                            resultText = "Document scan successful! Type: ${result.documentType}"
                        }
                        
                        override fun onError(error: SDKError) {
                            resultText = "Document scan failed: ${error.message}"
                        }
                        
                        override fun onCancelled() {
                            resultText = "Document scan cancelled"
                        }
                        
                        override fun onProgress(isDocumentDetected: Boolean, qualityScore: Float) {
                            resultText = "Document detected: $isDocumentDetected, Quality: ${(qualityScore * 100).toInt()}%"
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Document Scanning")
        }
        
        // Simple API calls (no callbacks)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    ArtiusIDSDK.startFaceLiveness(context as ComponentActivity)
                    resultText = "Face liveness started (simple API)"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Simple Face")
            }
            
            Button(
                onClick = {
                    ArtiusIDSDK.startDocumentScan(context as ComponentActivity)
                    resultText = "Document scan started (simple API)"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Simple Doc")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // SDK Info
        val versionInfo = ArtiusIDSDK.getVersionInfo()
        Text(
            text = "SDK Version: $versionInfo",
            style = MaterialTheme.typography.bodySmall
        )
        
        Text(
            text = "SDK Initialized: ${ArtiusIDSDK.isInitialized()}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
