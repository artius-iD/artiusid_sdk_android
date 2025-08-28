package com.artiusid.sdk.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * UI utility functions for responsive design
 */

/**
 * Get relative width in dp based on screen width
 */
@Composable
fun getRelativeWidthDp(percentage: Float): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    return screenWidth * percentage
}

/**
 * Get relative height in dp based on screen height
 */
@Composable
fun getRelativeHeightDp(percentage: Float): Dp {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    return screenHeight * percentage
}

/**
 * Get relative font size based on screen width
 */
@Composable
fun getRelativeFontSize(baseSize: Float): TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val scaleFactor = screenWidth / 360f // Base on 360dp width
    return (baseSize * scaleFactor).sp
}

/**
 * Convert dp to pixels
 */
@Composable
fun dpToPx(dp: Dp): Float {
    val density = LocalDensity.current
    return with(density) { dp.toPx() }
}

/**
 * Convert pixels to dp
 */
@Composable
fun pxToDp(px: Float): Dp {
    val density = LocalDensity.current
    return with(density) { px.toDp() }
}
