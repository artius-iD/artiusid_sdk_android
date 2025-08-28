package com.artiusid.sdk.ui.screens.verification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.sdk.ui.components.GradientBackground
import com.artiusid.sdk.sdk.ui.theme.SDKColors

/**
 * Verification Steps Screen - Starting point for verification flow
 */
@Composable
fun VerificationStepsScreen(
    onStartVerification: () -> Unit,
    onBack: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Identity Verification",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = SDKColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "We'll guide you through a few simple steps to verify your identity",
                fontSize = 16.sp,
                color = SDKColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onStartVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SDKColors.Yellow900
                )
            ) {
                Text(
                    text = "Start Verification",
                    color = SDKColors.WhiteA700,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text(
                    text = "Cancel",
                    color = SDKColors.TextSecondary
                )
            }
        }
    }
}