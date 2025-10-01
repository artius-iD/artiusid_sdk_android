/*
 * File: SplashScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.R
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.presentation.permissions.PermissionRequest
import com.artiusid.sdk.presentation.permissions.PermissionsHandler
import com.artiusid.sdk.ui.components.ThemedImage

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
        delay(3000) // Match iOS 3-second delay
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Gray900, Bluegray900),
                    start = androidx.compose.ui.geometry.Offset(0f, 1f),
                    end = androidx.compose.ui.geometry.Offset(0f, 0f)
                )
            )
    ) {
        // Logo centered in the screen - responsive
        ThemedImage(
            defaultResourceId = R.drawable.logo_ios,
            overrideKey = "brand_logo",
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.Fit
        )

        // Version text at the bottom - responsive
        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
} 