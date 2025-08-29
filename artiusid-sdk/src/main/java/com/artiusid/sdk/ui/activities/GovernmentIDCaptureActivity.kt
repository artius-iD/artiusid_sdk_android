package com.artiusid.sdk.ui.activities

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ui.theme.ArtiusIDSDKTheme

class GovernmentIDCaptureActivity : BaseSDKActivity() {
    
    private var currentSide by mutableStateOf("front")
    private var frontCaptured by mutableStateOf(false)
    private var backCaptured by mutableStateOf(false)
    private var progress by mutableStateOf(0f)
    private var instruction by mutableStateOf("Position the front of your ID in the frame")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startIDCapture()
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
                    text = "Government ID Capture",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (currentSide == "front") "Front Side" else "Back Side",
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
                                text = if (currentSide == "front") "🆔" else "📄",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text("ID Camera Preview")
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (frontCaptured) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Front ${if (frontCaptured) "✓" else ""}",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (backCaptured) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Back ${if (backCaptured) "✓" else ""}",
                            modifier = Modifier.padding(12.dp)
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
    
    private fun startIDCapture() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            captureFrontSide()
        }, 1000)
    }
    
    private fun captureFrontSide() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        for (i in 1..100) {
            handler.postDelayed({
                progress = i / 200f
                when {
                    i < 25 -> instruction = "Position the front of your ID in the frame"
                    i < 50 -> instruction = "Hold steady..."
                    i < 75 -> instruction = "Capturing front side..."
                    else -> instruction = "Processing front image..."
                }
                
                if (i == 100) {
                    frontCaptured = true
                    currentSide = "back"
                    instruction = "Now position the back of your ID"
                    
                    handler.postDelayed({
                        captureBackSide()
                    }, 1500)
                }
            }, i * 30L)
        }
    }
    
    private fun captureBackSide() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        for (i in 1..100) {
            handler.postDelayed({
                progress = 0.5f + (i / 200f)
                when {
                    i < 25 -> instruction = "Position the back of your ID in the frame"
                    i < 50 -> instruction = "Hold steady..."
                    i < 75 -> instruction = "Capturing back side..."
                    else -> instruction = "Processing back image..."
                }
                
                if (i == 100) {
                    backCaptured = true
                    instruction = "Both sides captured! Processing..."
                    
                    handler.postDelayed({
                        completeIDCapture()
                    }, 1000)
                }
            }, i * 30L)
        }
    }
    
    private fun completeIDCapture() {
        finishWithSuccess()
    }
}
