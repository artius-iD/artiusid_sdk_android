/*
 * File: IntroHomeScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.presentation.screens.document.DocumentType
import com.artiusid.sdk.ui.theme.*
import androidx.compose.ui.layout.ContentScale
import com.artiusid.sdk.ui.components.ThemedImage

@Composable
fun IntroHomeScreen(
    onNavigateToFaceVerification: () -> Unit,
    onNavigateToDocumentScan: (DocumentType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Bluegray900, Gray900),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1f, 1f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = getRelativeWidthDp(20.0f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo - scaled to match iOS (maxHeight: geo.size.height / 7.5)
            ThemedImage(
                defaultResourceId = R.drawable.logo_ios,
                overrideKey = "brand_logo",
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getRelativeHeightDp(120.0f)) // Approximate for height/7.5
                    .padding(
                        top = getRelativeHeightDp(20.0f),
                        bottom = getRelativeHeightDp(-14.0f)
                    ),
                contentScale = ContentScale.Fit
            )
            
            // Title text - matching iOS styling
            Text(
                text = "Welcome to",
                fontSize = getRelativeFontSize(32.0f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(getRelativeWidthDp(361.0f))
                    .padding(top = getRelativeHeightDp(28.0f))
            )
            
            // ArtiusID text with yellow highlight
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Artius",
                    fontSize = getRelativeFontSize(32.0f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "ID",
                    fontSize = getRelativeFontSize(32.0f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Yellow900
                )
            }
            
            // Intro Image - scaled to match iOS (maxHeight: geo.size.height / 4.5)
            ThemedImage(
                defaultResourceId = R.drawable.intro_home_view_image_ios,
                overrideKey = "intro_home_image",
                contentDescription = "Intro Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getRelativeHeightDp(200.0f)) // Approximate for height/4.5
                    .padding(
                        top = getRelativeHeightDp(20.0f),
                        bottom = getRelativeHeightDp(20.0f)
                    ),
                contentScale = ContentScale.Fit
            )
            
            // Verify Now Button - iOS style
            GoNextButton(
                onClick = onNavigateToFaceVerification,
                text = "Verify Now",
                modifier = Modifier.padding(
                    start = getRelativeWidthDp(20.0f),
                    end = getRelativeWidthDp(20.0f),
                    top = getRelativeHeightDp(12.0f)
                )
            )
            
            // Authenticate Button - iOS style
            GoNextButton(
                onClick = { onNavigateToDocumentScan(DocumentType.PASSPORT) },
                text = "Authenticate",
                isSecondary = true,
                modifier = Modifier.padding(
                    start = getRelativeWidthDp(20.0f),
                    end = getRelativeWidthDp(20.0f),
                    top = getRelativeHeightDp(12.0f)
                )
            )
            
            // Version view at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = getRelativeHeightDp(40.0f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
} 