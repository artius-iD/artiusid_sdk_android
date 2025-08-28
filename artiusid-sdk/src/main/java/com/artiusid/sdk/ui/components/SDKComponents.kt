package com.artiusid.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.sdk.ui.theme.SDKColors
import com.artiusid.sdk.sdk.ui.utils.*

/**
 * Gradient background composable
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SDKColors.BackgroundPrimary,
                        SDKColors.BackgroundSecondary
                    )
                )
            )
    ) {
        content()
    }
}

/**
 * Custom info button
 */
@Composable
fun CustomInfoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SDKColors.Yellow900,
    textColor: Color = SDKColors.WhiteA700
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * SDK Status indicator
 */
@Composable
fun SDKStatusIndicator(
    status: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (isSuccess) SDKColors.Success else SDKColors.Error,
                    shape = RoundedCornerShape(6.dp)
                )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = status,
            color = SDKColors.TextPrimary,
            fontSize = 14.sp
        )
    }
}

/**
 * SDK Loading indicator
 */
@Composable
fun SDKLoadingIndicator(
    message: String = "Processing...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = SDKColors.Yellow900,
            strokeWidth = 3.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            color = SDKColors.TextSecondary,
            fontSize = 14.sp
        )
    }
}
