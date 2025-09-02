package com.artiusid.sdk.ui.screens.document

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.ui.theme.*
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.GifDecoder
import androidx.compose.ui.platform.LocalContext

/**
 * PassportScanIntroScreen - EXACT STANDALONE APPLICATION IMPLEMENTATION
 * Matches the standalone app's passport scan intro screen exactly
 */
@Composable
fun PassportScanIntroScreen(
    onNavigateToPassportScan: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Configure Coil for GIF support - EXACT STANDALONE IMPLEMENTATION
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }
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
                navTitle = "Passport"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Passport Animation (GIF) - EXACT STANDALONE IMPLEMENTATION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                AsyncImage(
                    model = R.drawable.passport_animation,
                    contentDescription = "Passport Animation",
                    imageLoader = imageLoader,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title - EXACT STANDALONE IMPLEMENTATION
            Text(
                text = "Scan Passport",
                color = Color(0xFFF58220), // Yellow900 - EXACT STANDALONE COLOR
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description - EXACT STANDALONE IMPLEMENTATION
            Text(
                text = "Continue your verification by scanning your passport. See animation above and when ready tap Scan My Passport",
                color = Color(0xFFFFFFFF), // WhiteA700 - EXACT STANDALONE COLOR
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Scan button - EXACT STANDALONE IMPLEMENTATION
            Button(
                onClick = onNavigateToPassportScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF58220) // Yellow900 - EXACT STANDALONE COLOR
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Scan My Passport",
                    color = Color(0xFF18202A), // Gray900 - EXACT STANDALONE COLOR
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
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