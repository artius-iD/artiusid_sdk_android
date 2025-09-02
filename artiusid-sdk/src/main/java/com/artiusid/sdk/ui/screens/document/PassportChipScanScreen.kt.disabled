package com.artiusid.sdk.ui.screens.document

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artiusid.sdk.models.PassportNFCData
import com.artiusid.sdk.utils.ImageStorage
import kotlinx.coroutines.delay

// NFC Scan States
sealed class NFCScanState {
    object Initial : NFCScanState()
    object WaitingForNFC : NFCScanState()
    object Processing : NFCScanState()
    data class Success(val message: String) : NFCScanState()
    data class Error(val message: String) : NFCScanState()
}

@Composable
fun PassportChipScanScreen(
    onChipScanComplete: (PassportNFCData?) -> Unit,
    onNavigateBack: () -> Unit,
    mrzKey: String = "",
    viewModel: DocumentScanViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // NFC state management
    var nfcScanState by remember { mutableStateOf<NFCScanState>(NFCScanState.Initial) }
    var retryCount by remember { mutableStateOf(0) }
    
    // Check NFC availability
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val isNfcAvailable = nfcAdapter?.isEnabled == true
    
    // Initialize NFC scanning
    LaunchedEffect(Unit) {
        if (isNfcAvailable) {
            nfcScanState = NFCScanState.WaitingForNFC
            // Simulate NFC scanning process
            delay(2000)
            
            // Create passport data from MRZ scan (simulated)
            val passportData = PassportNFCData(
                firstName = "John",
                lastName = "Doe", 
                documentNumber = "123456789",
                nationality = "US",
                dateOfBirth = "1990-01-01"
            )
            
            nfcScanState = NFCScanState.Success("NFC scan completed")
            delay(1000)
            onChipScanComplete(passportData)
        } else {
            nfcScanState = NFCScanState.Error("NFC is not available or disabled")
        }
    }
    
    // Function to retry NFC scanning
    fun retryNfcScan() {
        retryCount += 1
        nfcScanState = NFCScanState.WaitingForNFC
    }
    
    // Function to skip NFC scanning
    fun skipNfcScan() {
        val passportData = PassportNFCData(
            firstName = "John",
            lastName = "Doe",
            documentNumber = "123456789", 
            nationality = "US",
            dateOfBirth = "1990-01-01"
        )
        onChipScanComplete(passportData)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("← Back")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = "NFC Chip Scan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status message
            Text(
                text = when (val state = nfcScanState) {
                    is NFCScanState.Initial -> "Initializing NFC..."
                    is NFCScanState.WaitingForNFC -> "Hold your passport near the device"
                    is NFCScanState.Processing -> "Reading passport chip..."
                    is NFCScanState.Success -> state.message
                    is NFCScanState.Error -> state.message
                },
                fontSize = 16.sp,
                color = when (nfcScanState) {
                    is NFCScanState.Error -> Color.Red
                    is NFCScanState.Success -> Color.Green
                    else -> Color.White
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // NFC Icon/Animation placeholder
            Card(
                modifier = Modifier
                    .size(120.dp),
                shape = RoundedCornerShape(60.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (nfcScanState) {
                        is NFCScanState.Processing -> Color.Blue
                        is NFCScanState.Success -> Color.Green
                        is NFCScanState.Error -> Color.Red
                        else -> Color.Gray
                    }
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NFC",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Action buttons
            when (nfcScanState) {
                is NFCScanState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { retryNfcScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry NFC Scan", color = Color.White)
                        }
                        
                        OutlinedButton(
                            onClick = { skipNfcScan() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Skip NFC Scan")
                        }
                    }
                }
                is NFCScanState.WaitingForNFC -> {
                    OutlinedButton(
                        onClick = { skipNfcScan() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Skip NFC Scan")
                    }
                }
                else -> {
                    // No buttons for other states
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Instructions:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Make sure NFC is enabled on your device\n• Hold your device flat against the passport\n• Keep steady until scan completes",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}