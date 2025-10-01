/*
 * File: DocumentScanBackIntroScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.ui.components.ThemedGifAnimation
import coil.compose.AsyncImage
import coil.compose.AsyncImage

@Composable
fun DocumentScanBackIntroScreen(
    onNavigateToDocumentScanBack: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.artiusid.sdk.ui.theme.ColorManager.getCurrentScheme().background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom back button
            CustomBackButton(
                onBackClick = onNavigateBack,
                navTitle = "Back ID"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // State ID Back Animation (GIF)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                ThemedGifAnimation(
                    defaultResourceId = R.drawable.stateid_animation,
                    overrideKey = "state_id_animation_gif",
                    contentDescription = "State ID Back Animation",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = "Scan Back ID",
                color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = "Continue your verification by scanning the back of your document. See animation above and when ready tap Scan My ID",
                color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Scan button
            Button(
                onClick = onNavigateToDocumentScanBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Scan Back of My ID",
                    color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonTextColor(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CustomBackButton(
    onBackClick: () -> Unit,
    navTitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor()
            )
        }
        
        Text(
            text = navTitle,
            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
} 