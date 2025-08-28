package com.artiusid.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.artiusid.sdk.ui.theme.SDKColors

/**
 * Gradient background component used throughout the standalone app
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
                        SDKColors.Bluegray900,
                        SDKColors.Bluegray901,
                        SDKColors.Bluegray902
                    )
                )
            )
    ) {
        content()
    }
}

/**
 * Alternative gradient background
 */
@Composable
fun GradientBackgroundLight(
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
