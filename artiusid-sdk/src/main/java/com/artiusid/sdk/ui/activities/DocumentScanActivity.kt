package com.artiusid.sdk.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.models.*
import com.artiusid.sdk.services.EnhancedCameraService
import com.artiusid.sdk.services.DocumentScanService
import com.artiusid.sdk.ui.theme.ArtiusIDSDKTheme
import kotlinx.coroutines.launch

/**
 * Activity for document scanning with real camera and OCR
 */
class DocumentScanActivity : BaseSDKActivity() {
    
    private lateinit var enhancedCameraService: EnhancedCameraService
    private lateinit var documentScanService: DocumentScanService
    private var documentType: DocumentType = DocumentType.PASSPORT
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startDocumentScan()
        } else {
            finishWithError(SDKError(
                code = SDKErrorCode.PERMISSION_DENIED,
                message = "Camera permission is required for document scanning"
            ))
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize services
        enhancedCameraService = EnhancedCameraService(this)
        documentScanService = DocumentScanService(this)
        
        // Get document type from intent
        documentType = DocumentType.valueOf(
            intent.getStringExtra("document_type") ?: DocumentType.PASSPORT.name
        )
        
        // Check camera permission
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startDocumentScan()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    @Composable
    override fun Content() {
        var instruction by remember { mutableStateOf("Position document in frame") }
        var isProcessing by remember { mutableStateOf(false) }
        var previewView by remember { mutableStateOf<PreviewView?>(null) }
        
        val cameraReady by enhancedCameraService.isCameraReady.collectAsState()
        val focusStable by enhancedCameraService.isFocusStable.collectAsState()
        
        ArtiusIDSDKTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Document Scanning",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Scan your ${documentType.name.lowercase().replace("_", " ")}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Real camera preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AndroidView(
                        factory = { context ->
                            PreviewView(context).also { preview ->
                                previewView = preview
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // Focus stability indicator
                if (cameraReady && focusStable) {
                    Text(
                        text = "📷 Focus Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { finishAsCancelled() },
                        enabled = !isProcessing
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            captureAndProcessDocument { newInstruction ->
                                instruction = newInstruction
                                isProcessing = newInstruction.contains("Processing")
                            }
                        },
                        enabled = cameraReady && !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Capture")
                        }
                    }
                }
            }
        }
        
        // Start camera when preview is ready
        LaunchedEffect(previewView) {
            previewView?.let { preview ->
                enhancedCameraService.startCamera(
                    lifecycleOwner = this@DocumentScanActivity,
                    previewView = preview
                )
            }
        }
    }
    
    private fun startDocumentScan() {
        // Camera will be started when preview is ready
        android.util.Log.d("DocumentScanActivity", "Document scan initialized for type: $documentType")
    }
    
    private fun captureAndProcessDocument(onInstructionUpdate: (String) -> Unit) {
        onInstructionUpdate("Capturing image...")
        
        enhancedCameraService.captureImage { bitmap ->
            if (bitmap != null) {
                onInstructionUpdate("Processing document...")
                
                lifecycleScope.launch {
                    try {
                        val result = documentScanService.scanDocument(bitmap, documentType)
                        
                        android.util.Log.d("DocumentScanActivity", "Document scan result: ${result.success}")
                        android.util.Log.d("DocumentScanActivity", "OCR data: ${result.extractedData}")
                        
                        if (result.success && result.extractedData.isNotEmpty()) {
                            onInstructionUpdate("Document scanned successfully!")
                            
                            // Store the captured image and OCR data
                            com.artiusid.sdk.utils.ImageStorage.setDocumentImage(bitmap)
                            com.artiusid.sdk.utils.ImageStorage.setFrontOcrData(result.extractedData)
                            
                            android.util.Log.d("DocumentScanActivity", "Stored OCR data: ${result.extractedData}")
                            
                            ArtiusIDSDK.documentScanCallback?.onDocumentScanComplete(result)
                            finishWithSuccess()
                        } else {
                            onInstructionUpdate("Could not read document. Try again.")
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                onInstructionUpdate("Position document in frame")
                            }, 2000)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DocumentScanActivity", "Error processing document", e)
                        onInstructionUpdate("Error processing document. Try again.")
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            onInstructionUpdate("Position document in frame")
                        }, 2000)
                    }
                }
            } else {
                onInstructionUpdate("Failed to capture image. Try again.")
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onInstructionUpdate("Position document in frame")
                }, 2000)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        enhancedCameraService.release()
        documentScanService.release()
    }
}
