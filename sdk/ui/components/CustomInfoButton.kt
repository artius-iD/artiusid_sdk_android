/*
 * File: CustomInfoButton.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

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
import com.artiusid.sdk.ui.theme.ThemedButtonColors
import com.artiusid.sdk.ui.theme.ThemedTextColors

@Composable
fun CustomInfoButton(
    buttonLabel: String,
    isSecondary: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSecondary) {
        ThemedButtonColors.getSecondaryButtonColor()
    } else {
        ThemedButtonColors.getPrimaryButtonColor()
    }
    
    val textColor = if (isSecondary) {
        ThemedButtonColors.getSecondaryButtonTextColor()
    } else {
        ThemedButtonColors.getPrimaryButtonTextColor()
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(59.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.58.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buttonLabel,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
} 