/*
 * File: AuthenticationSuccessScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 * 
 * Matches iOS AuthenticatedView.swift exactly
 */

package com.artiusid.sdk.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.ui.theme.AppColors
import com.artiusid.sdk.ui.theme.ColorManager
import com.artiusid.sdk.ui.theme.EnhancedThemeManager

@Composable
fun AuthenticationSuccessScreen(
    accountFullName: String? = null,
    onBackHome: () -> Unit
) {
    // Note: context and iconTheme removed since we're using direct drawable reference
    // Match iOS background gradient exactly
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
            
            // Match iOS: SuccessImageComponent with specific dimensions
            Box(
                modifier = Modifier
                    .width(315.dp)
                    .height(321.dp),
                contentAlignment = Alignment.Center
            ) {
                // Success image - use high-quality approval icon
                Image(
                    painter = painterResource(id = R.drawable.approval),
                    contentDescription = "Authenticated",
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(87.dp))
            
            // Match iOS: "Authenticated" text in secondary color
            Text(
                text = "Authenticated",
                color = AppColors.secondary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(39.dp)
            )
            
            // Match iOS: Personalized greeting if name available
            if (!accountFullName.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(26.dp))
                
                Text(
                    text = "Hello, $accountFullName",
                    color = AppColors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(344.dp)
                        .height(83.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(15.dp))
            
            // Match iOS: "Back Home" button
            Button(
                onClick = onBackHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.secondary
                ),
                modifier = Modifier
                    .width(353.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Back Home",
                    color = AppColors.buttonTextSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Match iOS: Version info at bottom
            Text(
                text = "artius.iD SDK v1.0",
                color = AppColors.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
