package com.artiusid.sdk.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ui.theme.SDKColors

/**
 * Loading indicator component used throughout the standalone app
 */
@Composable
fun LoadingIndicator(
    message: String = "Loading...",
    modifier: Modifier = Modifier,
    color: Color = SDKColors.Yellow900
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            color = SDKColors.WhiteA700,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Small loading indicator
 */
@Composable
fun SmallLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = SDKColors.Yellow900
) {
    CircularProgressIndicator(
        color = color,
        strokeWidth = 2.dp,
        modifier = modifier.size(24.dp)
    )
}

/**
 * Loading overlay
 */
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String = "Processing...",
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SDKColors.Gray900.copy(alpha = 0.9f)
                )
            ) {
                LoadingIndicator(
                    message = message,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}