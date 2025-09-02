package com.artiusid.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * CustomInfoButton - EXACT STANDALONE APPLICATION IMPLEMENTATION
 * Orange instruction button used in face liveness screen
 */
@Composable
fun CustomInfoButton(
    buttonLabel: String,
    isSecondary: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(59.dp)
            .background(
                color = if (isSecondary) Color.White else Color(0xFFFF6B35), // iOS-like orange - EXACT STANDALONE COLOR
                shape = RoundedCornerShape(12.58.dp) // EXACT STANDALONE CORNER RADIUS
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buttonLabel,
            color = if (isSecondary) Color(0xFFFF6B35) else Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}