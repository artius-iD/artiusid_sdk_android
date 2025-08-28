package com.artiusid.sdk.ui.activities

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.sdk.ui.theme.ArtiusIDSDKTheme

class PassportCaptureActivity : BaseSDKActivity() {
    
    private var currentStep by mutableStateOf("front")
    private var frontCaptured by mutableStateOf(false)
    private var mrzCaptured by mutableStateOf(false)
    private var nfcRead by mutableStateOf(false)
    private var progress by mutableStateOf(0f)
    private var instruction by mutableStateOf("Position your passport's photo page in the frame")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startPassportCapture()
    }
    
    @Composable
    override fun Content() {
        ArtiusIDSDKTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Passport Verification",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = when (currentStep) {
                        "front" -> "Photo Page Capture"
                        "mrz" -> "MRZ Reading"
                        "nfc" -> "NFC Reading"
                        else -> "Processing"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (currentStep) {
                                    "front" -> "📘"
                                    "mrz" -> "🔍"
                                    "nfc" -> "📡"
                                    else -> "⚙️"
                                },
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text(
                                text = when (currentStep) {
                                    "front" -> "Passport Camera View"
                                    "mrz" -> "MRZ Scanner"
                                    "nfc" -> "NFC Reader"
                                    else -> "Processing"
                                }
                            )
                        }
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
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (frontCaptured) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Photo ${if (frontCaptured) "✓" else ""}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (mrzCaptured) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "MRZ ${if (mrzCaptured) "✓" else ""}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (nfcRead) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "NFC ${if (nfcRead) "✓" else ""}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(onClick = { finishAsCancelled() }) {
                    Text("Cancel")
                }
            }
        }
    }
    
    private fun startPassportCapture() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            captureFrontPage()
        }, 1000)
    }
    
    private fun captureFrontPage() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        for (i in 1..100) {
            handler.postDelayed({
                progress = i / 300f
                when {
                    i < 25 -> instruction = "Position your passport's photo page in the frame"
                    i < 50 -> instruction = "Hold steady..."
                    i < 75 -> instruction = "Capturing photo page..."
                    else -> instruction = "Processing photo page..."
                }
                
                if (i == 100) {
                    frontCaptured = true
                    currentStep = "mrz"
                    instruction = "Now scanning the MRZ (Machine Readable Zone)"
                    
                    handler.postDelayed({
                        captureMRZ()
                    }, 1500)
                }
            }, i * 25L)
        }
    }
    
    private fun captureMRZ() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        for (i in 1..100) {
            handler.postDelayed({
                progress = 0.33f + (i / 300f)
                when {
                    i < 25 -> instruction = "Position the MRZ area in the frame"
                    i < 50 -> instruction = "Reading text..."
                    i < 75 -> instruction = "Parsing MRZ data..."
                    else -> instruction = "Validating passport information..."
                }
                
                if (i == 100) {
                    mrzCaptured = true
                    currentStep = "nfc"
                    instruction = "Please place your phone on the passport for NFC reading"
                    
                    handler.postDelayed({
                        readNFC()
                    }, 1500)
                }
            }, i * 25L)
        }
    }
    
    private fun readNFC() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        for (i in 1..100) {
            handler.postDelayed({
                progress = 0.66f + (i / 300f)
                when {
                    i < 25 -> instruction = "Searching for NFC chip..."
                    i < 50 -> instruction = "Reading passport data..."
                    i < 75 -> instruction = "Verifying digital signature..."
                    else -> instruction = "Completing NFC verification..."
                }
                
                if (i == 100) {
                    nfcRead = true
                    instruction = "All steps completed! Processing final results..."
                    
                    handler.postDelayed({
                        completePassportCapture()
                    }, 1000)
                }
            }, i * 25L)
        }
    }
    
    private fun completePassportCapture() {
        finishWithSuccess()
    }
}
