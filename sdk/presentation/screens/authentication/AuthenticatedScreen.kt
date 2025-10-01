/*
 * File: AuthenticatedScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.ui.theme.GradientBackground
import com.artiusid.sdk.ui.theme.Yellow900
import com.artiusid.sdk.utils.VerificationStateManager

/**
 * Matches iOS AuthenticatedView.swift exactly
 * Shows authentication success and allows return to home
 */
@Composable
fun AuthenticatedScreen(
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val verificationStateManager = remember { VerificationStateManager(context) }
    val accountFullName = verificationStateManager.getAccountFullName()
    
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Success Image (like iOS SuccessImageComponent)
            Image(
                painter = painterResource(id = R.drawable.img_success),
                contentDescription = "Authentication Success",
                modifier = Modifier
                    .size(width = 315.dp, height = 321.dp)
                    .padding(bottom = 40.dp)
            )
            
            // Success Title (like iOS auth_success)
            Text(
                text = "Authentication Successful",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // Welcome message with account name (like iOS Hello, accountFullName)
            if (!accountFullName.isNullOrBlank()) {
                Text(
                    text = "Hello, $accountFullName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Back to Home Button (like iOS GoNextButtonView with "Back Home")
            Button(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                ),
                shape = RoundedCornerShape(12.58.dp)
            ) {
                Text(
                    text = "Back Home",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}