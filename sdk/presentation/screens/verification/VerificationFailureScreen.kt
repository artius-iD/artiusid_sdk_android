/**
 * Author: Todd Bryant
 * Company: artius.iD
 * 
 * Verification failure screen matching iOS DocumentRecaptureNotificationView
 */
package com.artiusid.sdk.presentation.screens.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.data.model.VerificationFailureType
import com.artiusid.sdk.ui.theme.*

@Composable
fun VerificationFailureScreen(
    failureType: VerificationFailureType,
    onRetryClick: () -> Unit,
    onBackToHomeClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(com.artiusid.sdk.ui.theme.ColorManager.getCurrentScheme().background, com.artiusid.sdk.ui.theme.ColorManager.getCurrentScheme().surface),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1f, 1f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Error Icon - Red X Circle (matching iOS)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(com.artiusid.sdk.ui.theme.ThemedStatusColors.getErrorColor()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Error",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Main Title (matching iOS)
            Text(
                text = failureType.getTitle(context),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Error Explanation (matching iOS)
            Text(
                text = failureType.getMessage(context),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            

            
            Spacer(modifier = Modifier.weight(1f))
            
            // Single Action Button
            Button(
                onClick = if (failureType == VerificationFailureType.GENERAL) onBackToHomeClick else onRetryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = failureType.getButtonText(context),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
