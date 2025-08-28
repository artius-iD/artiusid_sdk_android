package com.artiusid.sdk.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.artiusid.sdk.sdk.ArtiusIDSDK
import com.artiusid.sdk.sdk.models.*
import com.artiusid.sdk.sdk.ui.theme.ArtiusIDSDKTheme

/**
 * Activity for face liveness detection
 */
class FaceLivenessActivity : BaseSDKActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startLivenessDetection()
        } else {
            finishWithError(SDKError(
                code = SDKErrorCode.PERMISSION_DENIED,
                message = "Camera permission is required for face liveness detection"
            ))
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check camera permission
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLivenessDetection()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    @Composable
    override fun Content() {
        var progress by remember { mutableStateOf(0f) }
        var instruction by remember { mutableStateOf("Position your face in the circle") }
        
        // Auto-advance the progress for demo
        LaunchedEffect(Unit) {
            for (i in 1..100) {
                kotlinx.coroutines.delay(50)
                progress = i / 100f
                when {
                    i < 20 -> instruction = "Position your face in the circle"
                    i < 40 -> instruction = "Hold still..."
                    i < 60 -> instruction = "Turn head slightly left"
                    i < 80 -> instruction = "Turn head slightly right"
                    else -> instruction = "Processing..."
                }
            }
        }
        
        ArtiusIDSDKTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Face Liveness Detection",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Simulated camera preview area
                Card(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Camera Preview")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(onClick = { finishAsCancelled() }) {
                    Text("Cancel")
                }
            }
        }
    }
    
    private fun startLivenessDetection() {
        android.util.Log.d("FaceLivenessActivity", "Starting liveness detection - will complete in 5 seconds")
        // Simulate liveness detection process - auto complete after 5 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            android.util.Log.d("FaceLivenessActivity", "Completing liveness detection with RESULT_SUCCESS: $RESULT_SUCCESS")
            
            val result = LivenessResult(
                success = true,
                confidence = 0.95f,
                livenessScore = 0.92f,
                processingTime = 5000L,
                sessionId = "liveness-${System.currentTimeMillis()}"
            )
            
            ArtiusIDSDK.livenessCallback?.onLivenessComplete(result)
            finishWithSuccess() // Don't pass complex object, just finish with success
        }, 5000) // Auto complete after 5 seconds
    }
}
