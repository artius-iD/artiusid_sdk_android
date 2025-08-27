package com.artiusid.sdk.sdk.ui.activities

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.artiusid.sdk.sdk.ArtiusIDSDK
import com.artiusid.sdk.sdk.callbacks.VerificationStep
import com.artiusid.sdk.sdk.config.VerificationConfig
import com.artiusid.sdk.sdk.models.*
import com.artiusid.sdk.sdk.utils.ImageStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Complete verification flow activity - handles the entire process internally
 * Face Liveness → Document Scan → NFC Reading → Results
 */
class VerificationFlowActivity : BaseSDKActivity() {
    
    private var currentStep by mutableStateOf(VerificationStep.FACE_LIVENESS)
    private var progress by mutableStateOf(0)
    private var statusMessage by mutableStateOf("Starting verification...")
    private var isProcessing by mutableStateOf(false)
    
    private var verificationConfig: VerificationConfig? = null
    private var livenessResult: LivenessResult? = null
    private var documentResult: DocumentScanResult? = null
    private var nfcResult: NFCPassportResult? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get configuration from intent
        verificationConfig = intent.getParcelableExtra("verification_config")
        
        // Start the verification flow
        startVerificationFlow()
    }
    
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Identity Verification",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress indicator
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "$progress% Complete",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Current step indicator
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
                        text = "Current Step:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    Text(
                        text = getStepDisplayName(currentStep),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isProcessing) {
                CircularProgressIndicator()
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { finishAsCancelled() },
                enabled = !isProcessing
            ) {
                Text("Cancel Verification")
            }
        }
    }
    
    private fun startVerificationFlow() {
        lifecycleScope.launch {
            try {
                isProcessing = true
                
                // Step 1: Face Liveness Detection
                if (verificationConfig?.enableFaceVerification == true) {
                    performFaceLiveness()
                }
                
                // Step 2: Document Scanning
                if (verificationConfig?.enableDocumentScanning == true) {
                    performDocumentScanning()
                }
                
                // Step 3: NFC Reading (if enabled)
                if (verificationConfig?.enableNFCReading == true) {
                    performNFCReading()
                }
                
                // Step 4: Process Results
                processResults()
                
            } catch (e: Exception) {
                handleError(e)
            } finally {
                isProcessing = false
            }
        }
    }
    
    private suspend fun performFaceLiveness() {
        currentStep = VerificationStep.FACE_LIVENESS
        statusMessage = "Preparing face verification..."
        progress = 10
        
        // Simulate ML Kit face detection process
        for (i in 1..8) {
            delay(1000)
            statusMessage = "Detecting face movement - Segment $i/8"
            progress = 10 + (i * 10)
            
            // Report progress to callback
            ArtiusIDSDK.getVerificationCallback()?.onProgress(currentStep, progress)
        }
        
        // Create mock liveness result (in real implementation, this comes from ML Kit)
        livenessResult = LivenessResult(
            isLive = true,
            confidence = 0.95f,
            faceImage = null, // Would be actual captured image
            segmentsCompleted = 8,
            totalSegments = 8,
            processingTime = 8000L,
            livenessScore = 0.95f,
            qualityScore = 0.88f,
            blinkDetected = true
        )
        
        statusMessage = "Face verification completed successfully"
    }
    
    private suspend fun performDocumentScanning() {
        currentStep = VerificationStep.DOCUMENT_SCAN
        statusMessage = "Preparing document scanner..."
        progress = 30
        
        delay(1000)
        statusMessage = "Position your document in the frame"
        progress = 40
        
        // Simulate document detection and OCR
        delay(2000)
        statusMessage = "Document detected - Processing..."
        progress = 50
        
        delay(1500)
        statusMessage = "Performing OCR and MRZ parsing..."
        progress = 60
        
        delay(1500)
        statusMessage = "Validating document data..."
        progress = 70
        
        // Create mock document result (in real implementation, this comes from ML Kit + OCR)
        documentResult = DocumentScanResult(
            documentType = com.artiusid.sdk.config.DocumentType.ID_CARD,
            frontImage = null, // Would be actual captured image
            backImage = null,
            extractedData = mapOf(
                "firstName" to "John",
                "lastName" to "Doe",
                "documentNumber" to "123456789",
                "dateOfBirth" to "1990-01-01",
                "expiryDate" to "2030-01-01"
            ),
            mrzData = MRZData(
                documentType = "ID",
                issuingCountry = "USA",
                documentNumber = "123456789",
                dateOfBirth = "900101",
                dateOfExpiry = "300101",
                nationality = "USA",
                sex = "M",
                surname = "DOE",
                givenNames = "JOHN",
                checkDigitsValid = true,
                rawMRZ = "IDUSA1234567890<<<<<<<<<<<<<<<\n9001011M3001011USA<<<<<<<<<<<6\nDOE<<JOHN<<<<<<<<<<<<<<<<<<<<<"
            ),
            qualityScore = 0.92f,
            processingTime = 5000L,
            ocrConfidence = 0.89f
        )
        
        statusMessage = "Document scanning completed successfully"
        ArtiusIDSDK.getVerificationCallback()?.onProgress(currentStep, 70)
    }
    
    private suspend fun performNFCReading() {
        currentStep = VerificationStep.NFC_READING
        statusMessage = "Preparing NFC reader..."
        progress = 75
        
        delay(1000)
        statusMessage = "Place passport on NFC reader"
        progress = 80
        
        delay(2000)
        statusMessage = "Reading passport data..."
        progress = 85
        
        delay(1500)
        statusMessage = "Validating security features..."
        progress = 90
        
        // Create mock NFC result (in real implementation, this comes from NFC reading)
        nfcResult = NFCPassportResult(
            isSuccessful = true,
            passportData = PassportData(
                documentNumber = "123456789",
                issuingCountry = "USA",
                nationality = "USA",
                surname = "DOE",
                givenNames = "JOHN",
                dateOfBirth = "1990-01-01",
                dateOfExpiry = "2030-01-01",
                sex = "M"
            ),
            faceImage = null, // Would be actual passport photo
            securityFeatures = SecurityFeatures(
                activeAuthentication = true,
                passiveAuthentication = true,
                chipAuthentication = true,
                securityScore = 0.96f
            ),
            processingTime = 4500L
        )
        
        statusMessage = "NFC reading completed successfully"
        ArtiusIDSDK.getVerificationCallback()?.onProgress(currentStep, 90)
    }
    
    private suspend fun processResults() {
        currentStep = VerificationStep.PROCESSING
        statusMessage = "Processing verification results..."
        progress = 95
        
        delay(1000)
        
        // Calculate overall verification score
        val overallScore = calculateOverallScore()
        
        // Create final verification result
        val verificationResult = VerificationResult(
            sessionId = "session_${System.currentTimeMillis()}",
            timestamp = java.util.Date(),
            isSuccessful = overallScore >= 0.8f,
            livenessResult = livenessResult,
            documentResult = documentResult,
            nfcResult = nfcResult,
            overallScore = overallScore,
            verificationId = "verification_${System.currentTimeMillis()}",
            processingTime = System.currentTimeMillis() - intent.getLongExtra("start_time", System.currentTimeMillis())
        )
        
        currentStep = VerificationStep.COMPLETED
        statusMessage = "Verification completed!"
        progress = 100
        
        delay(500)
        
        // Clear stored images
        ImageStorage.clearAll()
        
        // Return result to sample app via callback
        ArtiusIDSDK.getVerificationCallback()?.onSuccess(verificationResult)
        ArtiusIDSDK.clearVerificationCallback()
        
        finishWithSuccess()
    }
    
    private fun calculateOverallScore(): Float {
        var totalScore = 0f
        var components = 0
        
        livenessResult?.let {
            totalScore += it.livenessScore
            components++
        }
        
        documentResult?.let {
            totalScore += it.qualityScore
            components++
        }
        
        nfcResult?.let {
            totalScore += it.securityFeatures.securityScore
            components++
        }
        
        return if (components > 0) totalScore / components else 0f
    }
    
    private fun handleError(error: Exception) {
        val sdkError = SDKError(
            code = SDKError.ERROR_PROCESSING_FAILED,
            message = "Verification failed: ${error.message}",
            details = error.stackTraceToString(),
            recoverable = false
        )
        
        ArtiusIDSDK.getVerificationCallback()?.onError(sdkError)
        ArtiusIDSDK.clearVerificationCallback()
        
        finishWithError(sdkError.code, sdkError.message)
    }
    
    private fun getStepDisplayName(step: VerificationStep): String {
        return when (step) {
            VerificationStep.FACE_LIVENESS -> "Face Verification"
            VerificationStep.DOCUMENT_SCAN -> "Document Scanning"
            VerificationStep.NFC_READING -> "NFC Reading"
            VerificationStep.PROCESSING -> "Processing Results"
            VerificationStep.COMPLETED -> "Completed"
        }
    }
}
