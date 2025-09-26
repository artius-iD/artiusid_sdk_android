/*
 * File: CustomBackButton.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ui.theme.AppColors

@Composable
fun CustomBackButton(
    onBackClick: () -> Unit,
    navTitle: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AppColors.surface.copy(alpha = 0.3f))
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = AppColors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Navigation title
        if (navTitle.isNotEmpty()) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = navTitle,
                color = AppColors.secondary, // Use orange color for title like in iOS screenshot
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 