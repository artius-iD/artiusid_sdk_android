/*
 * File: ApprovalResponseScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Sample App Approval Response Screen
 * Shows the result of the approval decision (approved/denied)
 * Matches the standalone app's ApprovalResponseScreen functionality
 */
@Composable
fun ApprovalResponseScreen(
    response: String, // "approve" or "deny"
    requestId: Int?,
    onNavigateHome: () -> Unit
) {
    val isApproved = response.lowercase() == "approve"
    var isProcessing by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf("") }
    
    // Simulate sending the approval response to the server
    LaunchedEffect(Unit) {
        android.util.Log.d("ApprovalResponseScreen", "📤 Sending approval response to server...")
        android.util.Log.d("ApprovalResponseScreen", "📋 Request ID: $requestId")
        android.util.Log.d("ApprovalResponseScreen", "📋 Response: $response")
        
        // Simulate API call delay
        delay(2000)
        
        // Simulate successful response
        isProcessing = false
        resultMessage = if (isApproved) {
            "Your approval has been processed successfully."
        } else {
            "Your denial has been processed successfully."
        }
        
        android.util.Log.d("ApprovalResponseScreen", "✅ Approval response sent successfully")
        
        // TODO: In a real implementation, send the response to the server
        // val response = ApprovalResponseManager.sendApprovalResponse(requestId, response)
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            if (isProcessing) {
                // Processing State
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Processing Response...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Sending your ${if (isApproved) "approval" else "denial"} to the server",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
                
            } else {
                // Result State
                
                // Result Icon
                Icon(
                    painter = painterResource(
                        id = if (isApproved) android.R.drawable.ic_dialog_info else android.R.drawable.ic_dialog_alert
                    ),
                    contentDescription = if (isApproved) "Approved" else "Denied",
                    modifier = Modifier.size(120.dp),
                    tint = if (isApproved) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Result Title
                Text(
                    text = if (isApproved) "Request Approved" else "Request Denied",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isApproved) Color(0xFF4CAF50) else Color(0xFFF44336),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                )
                
                Spacer(modifier = Modifier.height(18.dp))
                
                // Result Message
                Text(
                    text = resultMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Request ID Display
                if (requestId != null) {
                    Text(
                        text = "Request ID: $requestId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Done Button
                Button(
                    onClick = onNavigateHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
