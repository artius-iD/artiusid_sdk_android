/*
 * File: FaceScanIntroScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.face

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.ui.components.ThemedIcon
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceScanIntroScreen(
    onNavigateToFaceScan: () -> Unit,
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
                navTitle = ""
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Face Scan Animation (GIF) - responsive positioning
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = R.drawable.face_rotation_ios,
                    contentDescription = "Face Scan Animation",
                    modifier = Modifier
                        .size(280.dp)
                        .background(Color.Transparent),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Title - responsive text and styling
            Text(
                text = "Position your face in the frame",
                color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description - responsive styling
            Text(
                text = "Make sure your face is clearly visible and well-lit",
                color = com.artiusid.sdk.ui.theme.ThemedTextColors.getSecondaryTextColor(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            // Tips Section matching iOS grid layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tips",
                    color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        top = getRelativeHeightDp(10f),
                        bottom = getRelativeHeightDp(6f)
                    )
                )
                
                // First row matching iOS grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // No Glasses
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(getRelativeWidthDp(180f))
                            .padding(8.dp)
                    ) {
                        ThemedIcon(
                            iconRes = R.drawable.no_glasses_icon,
                            contentDescription = "No Glasses",
                            overrideKey = "no_glasses_icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remove glasses",
                            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                            fontSize = getRelativeFontSize(16f).sp
                        )
                    }
                    
                    // No Hat
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(getRelativeWidthDp(180f))
                            .padding(8.dp)
                    ) {
                        ThemedIcon(
                            iconRes = R.drawable.no_hat_icon,
                            contentDescription = "No Hat",
                            overrideKey = "no_hat_icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remove hat",
                            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                            fontSize = getRelativeFontSize(16f).sp
                        )
                    }
                }
                
                // Second row matching iOS grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // No Mask
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(getRelativeWidthDp(180f))
                            .padding(8.dp)
                    ) {
                        ThemedIcon(
                            iconRes = R.drawable.no_mask_icon,
                            contentDescription = "No Mask",
                            overrideKey = "no_mask_icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remove mask",
                            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                            fontSize = getRelativeFontSize(16f).sp
                        )
                    }
                    
                    // Good Light
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(getRelativeWidthDp(180f))
                            .padding(8.dp)
                    ) {
                        ThemedIcon(
                            iconRes = R.drawable.good_light_icon,
                            contentDescription = "Good Light",
                            overrideKey = "good_light_icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Good lighting",
                            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                            fontSize = getRelativeFontSize(16f).sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Continue button - responsive positioning
            Button(
                onClick = {
                    android.util.Log.d("FaceScanIntroScreen", "Start Face Scan button clicked, navigating to FaceScanScreen")
                    onNavigateToFaceScan()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Start Face Scan",
                    color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonTextColor(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
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
        
        if (navTitle.isNotEmpty()) {
            Text(
                text = navTitle,
                color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
} 