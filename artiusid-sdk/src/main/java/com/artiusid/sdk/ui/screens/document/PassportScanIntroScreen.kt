package com.artiusid.presentation.screens.document

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

@Composable
fun PassportScanIntroScreen(
    onNavigateToPassportScan: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray900)
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
                navTitle = "Passport"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Passport Animation (GIF)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                AsyncImage(
                    model = R.drawable.passport_animation,
                    contentDescription = "Passport Animation",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = "Scan Passport",
                color = Yellow900,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = "Continue your verification by scanning your passport. See animation above and when ready tap Scan My Passport",
                color = WhiteA700,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Scan button
            Button(
                onClick = onNavigateToPassportScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow900
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Scan My Passport",
                    color = Gray900,
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
                tint = WhiteA700
            )
        }
        
        if (navTitle.isNotEmpty()) {
            Text(
                text = navTitle,
                color = WhiteA700,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
} 