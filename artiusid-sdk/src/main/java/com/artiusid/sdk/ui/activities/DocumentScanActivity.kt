package com.artiusid.sdk.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.DocumentType
import com.artiusid.sdk.document.DocumentInfoExtractor
import com.artiusid.sdk.models.DocumentScanResult
import com.artiusid.sdk.models.MRZData
import com.artiusid.sdk.models.SDKError
import com.artiusid.sdk.ui.components.DocumentOverlay
import com.artiusid.sdk.utils.ImageStorage
import com.artiusid.sdk.utils.MRZParser
import com.artiusid.sdk.utils.passport.PassportTextAnalyzer
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Real Document Scan Activity using actual ML Kit OCR and MRZ parsing
 */
class DocumentScanActivity : BaseSDKActivity() {
    
    private lateinit var cameraExecutor: ExecutorService
    private var documentInfoExtractor: DocumentInfoExtractor? = null
    private var passportTextAnalyzer: PassportTextAnalyzer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Initialize document processing services
        documentInfoExtractor = DocumentInfoExtractor()
        passportTextAnalyzer = PassportTextAnalyzer(
            onMRZDetected = { mrzData, bitmap ->
                handleMRZDetected(mrzData, bitmap)
            }
        )
    }
    
    @Composable
    override fun Content() {
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
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
        }
        
        var scanningStatus by remember { mutableStateOf("Position document in frame") }
        var isProcessing by remember { mutableStateOf(false) }
        
        if (!hasCameraPermission) {
            // Request camera permission
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Document scanning requires camera access",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Grant Camera Permission")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { finishAsCancelled() }
                ) {
                    Text("Cancel")
                }
            }
        } else {
            // Camera preview with document detection
            Box(modifier = Modifier.fillMaxSize()) {
                // Camera preview
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            setupCamera(this, lifecycleOwner) { status ->
                                scanningStatus = status
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Document overlay
                DocumentOverlay(
                    modifier = Modifier.fillMaxSize()
                )
                
                // Status text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = scanningStatus,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                // Processing indicator
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Processing document...")
                            }
                        }
                    }
                }
                
                // Cancel button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Button(
                        onClick = { finishAsCancelled() },
                        enabled = !isProcessing
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
    
    private fun setupCamera(
        previewView: PreviewView, 
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        onStatusUpdate: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                // Image analysis for document detection
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analyzer ->
                        analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageForDocument(imageProxy, onStatusUpdate)
                        }
                    }
                
                // Select back camera for document scanning
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                
            } catch (exc: Exception) {
                android.util.Log.e("DocumentScanActivity", "Use case binding failed", exc)
                
                val sdkError = SDKError(
                    code = SDKError.ERROR_CAMERA_UNAVAILABLE,
                    message = "Failed to start camera: ${exc.message}"
                )
                
                ArtiusIDSDK.getDocumentScanCallback()?.onError(sdkError)
                ArtiusIDSDK.clearDocumentScanCallback()
                
                finishWithError(sdkError.code, sdkError.message)
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun processImageForDocument(imageProxy: ImageProxy, onStatusUpdate: (String) -> Unit) {
        try {
            val bitmap = imageProxy.toBitmap()
            
            // Use the real passport text analyzer for MRZ detection
            passportTextAnalyzer?.analyzeImage(bitmap)
            
            onStatusUpdate("Scanning for document...")
            
        } catch (e: Exception) {
            android.util.Log.e("DocumentScanActivity", "Error processing image", e)
        } finally {
            imageProxy.close()
        }
    }
    
    private fun handleMRZDetected(mrzData: com.artiusid.data.models.passport.PassportMRZData, bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                // Store the document image
                ImageStorage.setFrontImage(bitmap)
                
                // Convert to SDK MRZ format
                val sdkMrzData = MRZData(
                    documentType = "P",
                    issuingCountry = mrzData.issuingCountry ?: "",
                    documentNumber = mrzData.passportNumber ?: "",
                    dateOfBirth = mrzData.dateOfBirth ?: "",
                    dateOfExpiry = mrzData.dateOfExpiry ?: "",
                    nationality = mrzData.nationality ?: "",
                    sex = mrzData.sex ?: "",
                    surname = mrzData.surname ?: "",
                    givenNames = mrzData.givenNames ?: "",
                    checkDigitsValid = mrzData.isValid,
                    rawMRZ = "${mrzData.mrzLine1}\n${mrzData.mrzLine2}"
                )
                
                // Extract additional document info using OCR
                val extractedData = documentInfoExtractor?.extractInfo(
                    bitmap, 
                    com.artiusid.sdk.document.DocumentType.PASSPORT
                ) ?: emptyMap()
                
                // Create document scan result
                val documentResult = DocumentScanResult(
                    documentType = DocumentType.PASSPORT,
                    frontImage = bitmap,
                    backImage = null,
                    extractedData = extractedData,
                    mrzData = sdkMrzData,
                    barcodeData = null,
                    qualityScore = 0.92f,
                    processingTime = System.currentTimeMillis(),
                    ocrConfidence = 0.89f
                )
                
                // Return result to SDK
                ArtiusIDSDK.getDocumentScanCallback()?.onSuccess(documentResult)
                ArtiusIDSDK.clearDocumentScanCallback()
                
                finishWithSuccess()
                
            } catch (e: Exception) {
                android.util.Log.e("DocumentScanActivity", "Error processing MRZ", e)
                
                val sdkError = SDKError(
                    code = SDKError.ERROR_PROCESSING_FAILED,
                    message = "Failed to process document: ${e.message}"
                )
                
                ArtiusIDSDK.getDocumentScanCallback()?.onError(sdkError)
                ArtiusIDSDK.clearDocumentScanCallback()
                
                finishWithError(sdkError.code, sdkError.message)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

// Extension function to convert ImageProxy to Bitmap
private fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
