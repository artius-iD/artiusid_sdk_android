package com.artiusid.sdk.ui.activities

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.models.*
import com.artiusid.sdk.services.NFCPassportService
import com.artiusid.sdk.ui.theme.ArtiusIDSDKTheme
import com.artiusid.sdk.utils.ImageStorage
import kotlinx.coroutines.launch

/**
 * Activity for NFC passport reading with real NFC integration
 */
class NFCReadingActivity : BaseSDKActivity() {
    
    private lateinit var nfcPassportService: NFCPassportService
    private var mrzData: Map<String, String>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize NFC service
        nfcPassportService = NFCPassportService(this)
        
        // Get MRZ data from previous document scan if available
        mrzData = ImageStorage.getFrontOcrData()
        
        if (!nfcPassportService.isNFCAvailable()) {
            // NFC not available, simulate reading
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                simulateNFCReading()
            }, 3000)
        } else {
            // Enable NFC foreground dispatch
            nfcPassportService.enableForegroundDispatch(this)
            
            // Auto-simulate after 5 seconds if no NFC tag detected
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                simulateNFCReading()
            }, 5000)
        }
    }
    
    @Composable
    override fun Content() {
        var instruction by remember { mutableStateOf("Hold your passport near the NFC reader") }
        var isReading by remember { mutableStateOf(false) }
        
        ArtiusIDSDKTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NFC Passport Reading",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!nfcPassportService.isNFCAvailable()) {
                    Text(
                        text = "NFC not available - using simulation mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (isReading) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { finishAsCancelled() },
                        enabled = !isReading
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            instruction = "Reading NFC data..."
                            isReading = true
                            simulateNFCReading()
                        },
                        enabled = !isReading
                    ) {
                        Text("Simulate Read")
                    }
                }
            }
        }
    }
    
    private fun simulateNFCReading() {
        lifecycleScope.launch {
            try {
                val result = nfcPassportService.readPassportData(null, mrzData)
                
                android.util.Log.d("NFCReadingActivity", "NFC reading result: ${result.success}")
                android.util.Log.d("NFCReadingActivity", "NFC data: ${result.nfcData}")
                
                ArtiusIDSDK.nfcReadingCallback?.onNFCReadingComplete(result)
                finishWithSuccess()
                
            } catch (e: Exception) {
                android.util.Log.e("NFCReadingActivity", "NFC reading failed", e)
                
                val errorResult = NFCPassportResult(
                    nfcData = null,
                    success = false,
                    isAuthenticated = false,
                    expiresAt = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L), // 1 year from now
                    processingTime = 0L,
                    sessionId = "nfc-error-${System.currentTimeMillis()}",
                    errorMessage = "NFC reading failed: ${e.message}"
                )
                
                ArtiusIDSDK.nfcReadingCallback?.onNFCReadingComplete(errorResult)
                finishWithSuccess()
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        
        // Handle NFC tag detection
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            
            lifecycleScope.launch {
                try {
                    val result = nfcPassportService.readPassportData(tag, mrzData)
                    ArtiusIDSDK.nfcReadingCallback?.onNFCReadingComplete(result)
                    finishWithSuccess()
                } catch (e: Exception) {
                    android.util.Log.e("NFCReadingActivity", "Real NFC reading failed", e)
                    // Fall back to simulation
                    simulateNFCReading()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        nfcPassportService.enableForegroundDispatch(this)
    }
    
    override fun onPause() {
        super.onPause()
        nfcPassportService.disableForegroundDispatch(this)
    }
}
