/*
 * File: HomeScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.utils.VerificationStateManager

@Composable
fun HomeScreen(
    onNavigateToVerificationSteps: () -> Unit,
    onNavigateToAuthentication: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLivenessDebug: () -> Unit = {}
) {
    val context = LocalContext.current
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    // Check verification state like iOS keychain check
    val verificationStateManager = remember { VerificationStateManager(context) }
    val isVerified by remember { mutableStateOf(verificationStateManager.isVerified()) }
    val accountFullName by remember { mutableStateOf(verificationStateManager.getAccountFullName()) }
    val isAuthenticationEnabled = isVerified && !accountFullName.isNullOrBlank()

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo with triple tap to settings - responsive sizing
            Image(
                painter = painterResource(id = R.drawable.logo_ios),
                contentDescription = stringResource(R.string.content_desc_logo),
                modifier = Modifier
                    .size(120.dp)
                    .padding(top = 20.dp)
                    .clickable {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 1000) {
                            tapCount++
                            if (tapCount >= 3) {
                                onNavigateToSettings()
                                tapCount = 0
                            }
                        } else {
                            tapCount = 1
                        }
                        lastTapTime = currentTime
                    },
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Title - responsive text and styling
            Text(
                text = stringResource(R.string.welcome_to),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name_artius),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.app_name_id),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Intro Image - responsive sizing
            Image(
                painter = painterResource(id = R.drawable.intro_home_view_image_ios),
                contentDescription = stringResource(R.string.content_desc_intro_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 20.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 'Verify Now' Button - responsive dimensions
            Button(
                onClick = onNavigateToVerificationSteps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.button_verify_now),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 'Authenticate' Button - responsive dimensions and behavior
            Button(
                onClick = { 
                    if (isAuthenticationEnabled) {
                        onNavigateToAuthentication()
                    }
                },
                enabled = isAuthenticationEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAuthenticationEnabled) com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor() else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    contentColor = if (isAuthenticationEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.button_authenticate),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Authentication status text - matches iOS logic
            if (!isAuthenticationEnabled) {
                Text(
                    text = if (!isVerified) stringResource(R.string.auth_verification_required) else stringResource(R.string.auth_verification_completed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(R.string.welcome_user, accountFullName ?: stringResource(R.string.unknown_user)),
                    style = MaterialTheme.typography.bodySmall,
                    color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Version Text - responsive positioning
            Text(
                text = "${stringResource(R.string.version_label)} 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
} 