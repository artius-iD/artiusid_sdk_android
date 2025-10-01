/*
 * File: ButtonStyles.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

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
 * iOS-style button components matching the exact styling from iOS app
 */

@Composable
fun GoNextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isDisabled: Boolean = false,
    isSecondary: Boolean = false,
    topPadding: Float = 50.0f
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(getRelativeWidthDp(353.0f))
            .height(getRelativeHeightDp(59.0f))
            .padding(top = getRelativeHeightDp(topPadding)),
        enabled = !isDisabled,
        colors = if (isSecondary) {
            AppButtonDefaults.secondaryButtonColors()
        } else {
            AppButtonDefaults.primaryButtonColors()
        },
        shape = RoundedCornerShape(12.58.dp)
    ) {
        Text(
            text = text,
            fontSize = getRelativeFontSize(18.0f).sp,
            fontWeight = FontWeight.Bold,
            color = if (isSecondary) AppColors.buttonTextSecondary else AppColors.buttonTextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GetStartedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(getRelativeWidthDp(353.0f))
            .height(getRelativeHeightDp(59.0f))
            .padding(top = getRelativeHeightDp(50.0f)),
        colors = AppButtonDefaults.primaryButtonColors(),
        shape = RoundedCornerShape(12.58.dp)
    ) {
        Text(
            text = text,
            fontSize = getRelativeFontSize(18.0f).sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.buttonTextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CustomInfoButton(
    buttonLabel: String,
    isSecondary: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = ColorManager.getCurrentScheme().surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = buttonLabel,
            modifier = Modifier.padding(12.dp),
            color = ColorManager.getCurrentScheme().textPrimary,
            fontSize = getRelativeFontSize(16.0f).sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
} 