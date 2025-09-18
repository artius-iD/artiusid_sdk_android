/*
 * File: Dimensions.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Responsive viewport scaling functions
 * Based on iOS baseline: 393px width, 852px height
 * Now includes bounds checking for better responsiveness
 */

@Composable
fun getRelativeWidth(size: Float): Float {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val scale = (screenWidth / 393f).coerceIn(0.8f, 1.4f) // Bounded scaling
    return size * scale
}

@Composable
fun getRelativeHeight(size: Float): Float {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val scale = (screenHeight / 852f).coerceIn(0.8f, 1.4f) // Bounded scaling
    return size * scale * 0.97f
}

@Composable
fun getRelativeFontSize(size: Float): Float {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val scale = (screenWidth / 393f).coerceIn(0.9f, 1.2f) // More conservative font scaling
    return size * scale
}

// Convert to dp for Compose
@Composable
fun getRelativeWidthDp(size: Float) = getRelativeWidth(size).dp

@Composable
fun getRelativeHeightDp(size: Float) = getRelativeHeight(size).dp

@Composable
fun getRelativeFontSizeSp(size: Float) = getRelativeFontSize(size).dp 