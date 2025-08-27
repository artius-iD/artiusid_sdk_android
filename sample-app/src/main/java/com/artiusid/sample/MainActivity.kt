package com.artiusid.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.callbacks.*
import com.artiusid.sdk.config.*
import com.artiusid.sdk.models.*

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
            .setApiKey("sample-api-key-12345")
            .setEnvironment(Environment.DEVELOPMENT)
            .setBrandingConfig(
                BrandingConfig(
                    colorScheme = ColorScheme.LIGHT,
                    companyName = "Sample Company Inc.",
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
                    enableNFCReading = true, // Enable all features for demo
                    livenessConfig = LivenessConfig(
                        segmentCount = 8,
                        headMovementThreshold = 5.0f,
                        requireBlink = true
                    )
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
    var isProcessing by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ArtiusID SDK Demo",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Text(
            text = "Complete Identity Verification Solution",
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
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Complete Verification Flow - THE MAIN FEATURE
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔐 Complete Verification Flow",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = "Face Liveness + Document Scan + NFC Reading",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        isProcessing = true
                        resultText = "Starting complete verification flow..."
                        
                        ArtiusIDSDK.startVerificationFlow(
                            activity = context as ComponentActivity,
                            callback = object : VerificationCallback {
                                override fun onSuccess(result: VerificationResult) {
                                    isProcessing = false
                                    val summary = buildString {
                                        appendLine("🎉 VERIFICATION COMPLETED SUCCESSFULLY!")
                                        appendLine("=" .repeat(50))
                                        appendLine("Session ID: ${result.sessionId}")
                                        appendLine("Verification ID: ${result.verificationId}")
                                        appendLine("Overall Score: ${(result.overallScore * 100).toInt()}%")
                                        appendLine("Processing Time: ${result.processingTime}ms")
                                        appendLine("Timestamp: ${result.timestamp}")
                                        appendLine()
                                        
                                        result.livenessResult?.let { liveness ->
                                            appendLine("👤 FACE LIVENESS RESULTS:")
                                            appendLine("  ✅ Live Detection: ${liveness.isLive}")
                                            appendLine("  📊 Liveness Score: ${(liveness.livenessScore * 100).toInt()}%")
                                            appendLine("  🎯 Quality Score: ${(liveness.qualityScore * 100).toInt()}%")
                                            appendLine("  🔄 Segments Completed: ${liveness.segmentsCompleted}/${liveness.totalSegments}")
                                            appendLine("  👁️ Blink Detected: ${liveness.blinkDetected}")
                                            appendLine("  ⏱️ Processing Time: ${liveness.processingTime}ms")
                                            appendLine()
                                        }
                                        
                                        result.documentResult?.let { doc ->
                                            appendLine("📄 DOCUMENT SCAN RESULTS:")
                                            appendLine("  📋 Document Type: ${doc.documentType}")
                                            appendLine("  🎯 Quality Score: ${(doc.qualityScore * 100).toInt()}%")
                                            appendLine("  🔍 OCR Confidence: ${(doc.ocrConfidence * 100).toInt()}%")
                                            appendLine("  ⏱️ Processing Time: ${doc.processingTime}ms")
                                            appendLine("  📝 Extracted Data:")
                                            doc.extractedData.forEach { (key, value) ->
                                                appendLine("    • $key: $value")
                                            }
                                            doc.mrzData?.let { mrz ->
                                                appendLine("  🔐 MRZ Data:")
                                                appendLine("    • Document: ${mrz.documentNumber}")
                                                appendLine("    • Name: ${mrz.givenNames} ${mrz.surname}")
                                                appendLine("    • Country: ${mrz.issuingCountry}")
                                                appendLine("    • Valid: ${mrz.checkDigitsValid}")
                                            }
                                            appendLine()
                                        }
                                        
                                        result.nfcResult?.let { nfc ->
                                            appendLine("📱 NFC READING RESULTS:")
                                            appendLine("  ✅ Success: ${nfc.isSuccessful}")
                                            appendLine("  🛡️ Security Score: ${(nfc.securityFeatures.securityScore * 100).toInt()}%")
                                            appendLine("  ⏱️ Processing Time: ${nfc.processingTime}ms")
                                            appendLine("  🔐 Security Features:")
                                            appendLine("    • Active Auth: ${nfc.securityFeatures.activeAuthentication}")
                                            appendLine("    • Passive Auth: ${nfc.securityFeatures.passiveAuthentication}")
                                            appendLine("    • Chip Auth: ${nfc.securityFeatures.chipAuthentication}")
                                            appendLine("  📋 Passport Data:")
                                            appendLine("    • Name: ${nfc.passportData.givenNames} ${nfc.passportData.surname}")
                                            appendLine("    • Document: ${nfc.passportData.documentNumber}")
                                            appendLine("    • Country: ${nfc.passportData.issuingCountry}")
                                            appendLine()
                                        }
                                        
                                        appendLine("🎯 VERIFICATION SUMMARY:")
                                        appendLine("  Status: ${if (result.isSuccessful) "✅ PASSED" else "❌ FAILED"}")
                                        appendLine("  Confidence: ${(result.overallScore * 100).toInt()}%")
                                        appendLine("  All data processed and validated within SDK!")
                                    }
                                    resultText = summary
                                }
                                
                                override fun onError(error: SDKError) {
                                    isProcessing = false
                                    resultText = buildString {
                                        appendLine("❌ VERIFICATION FAILED")
                                        appendLine("Error Code: ${error.code}")
                                        appendLine("Message: ${error.message}")
                                        appendLine("Details: ${error.details ?: "None"}")
                                        appendLine("Recoverable: ${error.recoverable}")
                                        appendLine("Timestamp: ${error.timestamp}")
                                    }
                                }
                                
                                override fun onCancelled() {
                                    isProcessing = false
                                    resultText = "⚠️ Verification cancelled by user"
                                }
                                
                                override fun onProgress(step: VerificationStep, progress: Int) {
                                    resultText = "�� Progress: $step - $progress%\n\nAll processing happening inside SDK..."
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Start Complete Verification")
                }
            }
        }
        
        // Individual Components (for testing)
        Text(
            text = "Individual Components:",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    ArtiusIDSDK.startFaceLiveness(
                        activity = context as ComponentActivity,
                        callback = object : LivenessCallback {
                            override fun onSuccess(result: LivenessResult) {
                                resultText = "✅ Face Liveness: ${(result.livenessScore * 100).toInt()}% confidence"
                            }
                            override fun onError(error: SDKError) {
                                resultText = "❌ Face Liveness failed: ${error.message}"
                            }
                            override fun onCancelled() {
                                resultText = "⚠️ Face Liveness cancelled"
                            }
                        }
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !isProcessing
            ) {
                Text("Face Only")
            }
            
            Button(
                onClick = {
                    ArtiusIDSDK.startDocumentScan(
                        activity = context as ComponentActivity,
                        documentType = DocumentType.ID_CARD,
                        callback = object : DocumentScanCallback {
                            override fun onSuccess(result: DocumentScanResult) {
                                resultText = "✅ Document Scan: ${result.documentType} - ${(result.qualityScore * 100).toInt()}% quality"
                            }
                            override fun onError(error: SDKError) {
                                resultText = "❌ Document Scan failed: ${error.message}"
                            }
                            override fun onCancelled() {
                                resultText = "⚠️ Document Scan cancelled"
                            }
                        }
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !isProcessing
            ) {
                Text("Doc Only")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // SDK Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "SDK Information",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Version: ${ArtiusIDSDK.getVersionInfo()}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "Initialized: ${ArtiusIDSDK.isInitialized()}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "Features: Face Liveness, Document Scan, NFC Reading",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "All processing contained within SDK",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
