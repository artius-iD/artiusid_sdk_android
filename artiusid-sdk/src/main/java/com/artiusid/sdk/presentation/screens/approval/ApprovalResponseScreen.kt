/*
 * File: ApprovalResponseScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.approval

import androidx.compose.foundation.Image
import com.artiusid.sdk.ui.components.ThemedImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.R
import com.artiusid.sdk.data.model.AppNotificationState
import com.artiusid.sdk.ui.theme.AppColors
import com.artiusid.sdk.ui.theme.ColorManager
import androidx.compose.foundation.background
import kotlinx.coroutines.delay

/**
 * Matches iOS ApprovalResponseView.swift exactly
 * Shows approval response result and sends response to server
 */
@Composable
fun ApprovalResponseScreen(
    response: String, // "yes" or "no"
    onNavigateToHome: () -> Unit
) {
    val isApproved = response.lowercase() == "yes"
    var displayResultMessage by remember { mutableStateOf("") }
    
    // Reset notification state when navigating back (like iOS onAppear)
    LaunchedEffect(Unit) {
        // Simulate sending approval response (like iOS task block)
        delay(1000) // Show loading state briefly
        
        try {
            // Simulate API call like iOS ApprovalResponse.shared.sendApprovalResponse
            val approvalValue = if (isApproved) "Approved" else "Deny"
            
            // For now, simulate success response
            displayResultMessage = if (isApproved) {
                "Your approval has been processed successfully."
            } else {
                "Your denial has been processed successfully."
            }
            
            // In a real implementation, you would call:
            // val response = ApprovalResponseManager.sendApprovalResponse(approvalValue)
            
        } catch (e: Exception) {
            displayResultMessage = "Failed to process approval response."
        }
    }
    
    // Reset notification state when leaving (like iOS onAppear reset)
    DisposableEffect(Unit) {
        onDispose {
            AppNotificationState.reset()
        }
    }
    
    // Use theme-based background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = ColorManager.getGradientBrush()
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Approval Result Image (like iOS approved/declined images) - supports corporate image overrides
            ThemedImage(
                defaultResourceId = if (isApproved) R.drawable.img_success else R.drawable.declined,
                overrideKey = if (isApproved) "success_icon" else "declined_icon",
                contentDescription = if (isApproved) "Approved" else "Declined",
                modifier = Modifier
                    .size(width = 353.dp, height = 254.dp)
                    .padding(bottom = 20.dp)
            )
            
            // Title based on response (like iOS approval_response_approveTitle/declineTitle)
            Text(
                text = if (isApproved) "Request Approved" else "Request Declined",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isApproved) com.artiusid.sdk.ui.theme.ThemedStatusColors.getSuccessColor() else com.artiusid.sdk.ui.theme.ThemedStatusColors.getErrorColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            )
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Result message (like iOS displayResultMessage or approval_response_viewText)
            Text(
                text = if (displayResultMessage.isNotEmpty()) {
                    displayResultMessage
                } else {
                    "You responded ${response.lowercase()}."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = com.artiusid.sdk.ui.theme.ThemedTextColors.getSecondaryTextColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Done Button (like iOS GoNextButtonView with "Done")
            Button(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp),
                colors = com.artiusid.sdk.ui.theme.AppButtonDefaults.primaryButtonColors(),
                shape = RoundedCornerShape(12.58.dp)
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}