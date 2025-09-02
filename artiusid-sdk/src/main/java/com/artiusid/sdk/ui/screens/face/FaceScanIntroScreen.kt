package com.artiusid.sdk.ui.screens.face

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage

/**
 * FaceScanIntroScreen - EXACT STANDALONE APPLICATION IMPLEMENTATION
 * Matches the standalone app's face scan intro screen exactly
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceScanIntroScreen(
    onNavigateToFaceScan: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18202A)) // Gray900 - EXACT STANDALONE COLOR
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom back button - EXACT STANDALONE IMPLEMENTATION
            CustomBackButton(
                onBackClick = onNavigateBack,
                navTitle = ""
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Face Scan Animation (GIF) - responsive positioning - EXACT STANDALONE IMPLEMENTATION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = R.drawable.face_rotation_ios,
                    contentDescription = "Face Scan Animation",
                    modifier = Modifier.size(280.dp), // EXACT STANDALONE SIZE
                    contentScale = ContentScale.Fit
                )
            }
            
            // Title - responsive text and styling - EXACT STANDALONE IMPLEMENTATION
            Text(
                text = "Position your face in the frame",
                color = Color(0xFFF58220), // Yellow900 - EXACT STANDALONE COLOR
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description - responsive styling - EXACT STANDALONE IMPLEMENTATION
            Text(
                text = "Make sure your face is clearly visible and well-lit",
                color = Color(0xFF9E9E9E), // Gray500 - EXACT STANDALONE COLOR
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            // Tips Section matching iOS grid layout - EXACT STANDALONE IMPLEMENTATION
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tips",
                    color = Color(0xFFFFFFFF), // WhiteA700 - EXACT STANDALONE COLOR
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        top = 10.dp,
                        bottom = 6.dp
                    )
                )
                
                // First row matching iOS grid - EXACT STANDALONE IMPLEMENTATION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // No Glasses
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(180.dp)
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.no_glasses_icon),
                            contentDescription = "No Glasses",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remove glasses",
                            color = Color(0xFFFFFFFF), // WhiteA700
                            fontSize = 16.sp
                        )
                    }
                    
                    // No Hat
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(180.dp)
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.no_hat_icon),
                            contentDescription = "No Hat",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remove hat",
                            color = Color(0xFFFFFFFF), // WhiteA700
                            fontSize = 16.sp
                        )
                    }
                }
                
                // Second row matching iOS grid - EXACT STANDALONE IMPLEMENTATION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // No Mask
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(180.dp)
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.no_mask_icon),
                            contentDescription = "No Mask",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remove mask",
                            color = Color(0xFFFFFFFF), // WhiteA700
                            fontSize = 16.sp
                        )
                    }
                    
                    // Good Light
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(180.dp)
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.good_light_icon),
                            contentDescription = "Good Light",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Good lighting",
                            color = Color(0xFFFFFFFF), // WhiteA700
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Continue button - responsive positioning - EXACT STANDALONE IMPLEMENTATION
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
                    containerColor = Color(0xFFF58220) // Yellow900 - EXACT STANDALONE COLOR
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Start Face Scan",
                    color = Color(0xFF18202A), // Gray900 - EXACT STANDALONE COLOR
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * CustomBackButton - EXACT STANDALONE APPLICATION IMPLEMENTATION
 */
@Composable
fun CustomBackButton(
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
                tint = Color(0xFFFFFFFF) // WhiteA700 - EXACT STANDALONE COLOR
            )
        }
        
        if (navTitle.isNotEmpty()) {
            Text(
                text = navTitle,
                color = Color(0xFFFFFFFF), // WhiteA700 - EXACT STANDALONE COLOR
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}