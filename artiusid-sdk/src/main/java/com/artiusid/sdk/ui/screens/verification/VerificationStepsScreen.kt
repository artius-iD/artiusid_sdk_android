package com.artiusid.sdk.ui.screens.verification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

/**
 * Verification Steps Screen - EXACT standalone app UI/UX
 * This is the first screen users see when starting verification
 */
@Composable
fun VerificationStepsScreen(
    onStartVerification: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E3A8A)) // Dark blue background like in screenshot
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom back button and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Verification Steps",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main title
            Text(
                text = "Follow these steps to verify your identity",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Step 1: Face Scan
            VerificationStepItem(
                icon = Icons.Default.Face,
                title = "Face Scan",
                description = "Scan your face for verification",
                iconColor = Color(0xFFFF9500) // Orange color
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Step 2: Document Scan  
            VerificationStepItem(
                icon = Icons.Default.CreditCard,
                title = "Document Scan",
                description = "Scan your ID document",
                iconColor = Color(0xFFFF9500) // Orange color
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Step 3: Completion
            VerificationStepItem(
                icon = Icons.Default.CheckCircle,
                title = "Completion",
                description = "Complete the verification process",
                iconColor = Color(0xFFFF9500) // Orange color
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Start Now button - EXACT orange color from screenshot
            Button(
                onClick = onStartVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500) // Orange color from screenshot
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Start Now",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun VerificationStepItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with orange color
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = iconColor, // Orange title
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}