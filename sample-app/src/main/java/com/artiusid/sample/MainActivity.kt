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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        ArtiusIDSDK.initialize(this, "sample-api-key")
        
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
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                ArtiusIDSDK.startFaceLiveness(context as ComponentActivity)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Face Liveness Detection")
        }
        
        Button(
            onClick = {
                ArtiusIDSDK.startDocumentScan(context as ComponentActivity)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Document Scanning")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "SDK Version: ${ArtiusIDSDK.getVersionInfo()}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
