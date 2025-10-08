package com.example.yourapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.SDKError

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        val config = SDKConfiguration.Builder()
            .setEnvironment(SDKConfiguration.Environment.PRODUCTION)
            .build()
            
        ArtiusIDSDK.initialize(this, config)
        
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
    
    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { startVerification() }
            ) {
                Text("Start Identity Verification")
            }
        }
    }
    
    private fun startVerification() {
        ArtiusIDSDK.startVerificationFlow(
            activity = this,
            callback = object : VerificationCallback {
                override fun onSuccess(result: VerificationResult) {
                    // Handle successful verification
                    println("Verification successful: ${result.verificationId}")
                }
                
                override fun onError(error: SDKError) {
                    // Handle error
                    println("Verification error: ${error.message}")
                }
                
                override fun onCancelled() {
                    // Handle cancellation
                    println("Verification cancelled")
                }
            }
        )
    }
}
