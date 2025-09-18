/*
 * File: ColorSchemes.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color scheme data class for SDK theming
 */
data class SDKColorScheme(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val success: Color,
    val error: Color,
    val warning: Color,
    val info: Color,
    val border: Color,
    val borderFocus: Color,
    val overlay: Color,
    val aligned: Color,
    val misaligned: Color,
    val segmentComplete: Color,
    val segmentIncomplete: Color
)

/**
 * Light color scheme
 */
fun LightColorScheme(): SDKColorScheme {
    return SDKColorScheme(
        primary = Color(0xFF6200EE),
        secondary = Color(0xFF03DAC5),
        background = Color.White,
        surface = Color.White,
        textPrimary = Color.Black,
        textSecondary = Color(0xFF757575),
        textDisabled = Color(0xFFBDBDBD),
        success = Color(0xFF4CAF50),
        error = Color(0xFFB00020),
        warning = Color(0xFFFFC107),
        info = Color(0xFF2196F3),
        border = Color(0xFFE0E0E0),
        borderFocus = Color(0xFF6200EE),
        overlay = Color(0x80000000),
        aligned = Color(0xFF4CAF50),
        misaligned = Color(0xFFFF5722),
        segmentComplete = Color(0xFF4CAF50),
        segmentIncomplete = Color(0xFFE0E0E0)
    )
}

/**
 * Dark color scheme
 */
fun DarkColorScheme(): SDKColorScheme {
    return SDKColorScheme(
        primary = Color(0xFFBB86FC),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        textPrimary = Color.White,
        textSecondary = Color(0xFFB3B3B3),
        textDisabled = Color(0xFF666666),
        success = Color(0xFF4CAF50),
        error = Color(0xFFCF6679),
        warning = Color(0xFFFFC107),
        info = Color(0xFF2196F3),
        border = Color(0xFF333333),
        borderFocus = Color(0xFFBB86FC),
        overlay = Color(0x80000000),
        aligned = Color(0xFF4CAF50),
        misaligned = Color(0xFFFF5722),
        segmentComplete = Color(0xFF4CAF50),
        segmentIncomplete = Color(0xFF333333)
    )
}

/**
 * Alternative color scheme
 */
fun AlternativeColorScheme(): SDKColorScheme {
    return SDKColorScheme(
        primary = Color(0xFF009688),
        secondary = Color(0xFFFF9800),
        background = Color(0xFFF5F5F5),
        surface = Color.White,
        textPrimary = Color(0xFF212121),
        textSecondary = Color(0xFF757575),
        textDisabled = Color(0xFFBDBDBD),
        success = Color(0xFF4CAF50),
        error = Color(0xFFF44336),
        warning = Color(0xFFFFC107),
        info = Color(0xFF2196F3),
        border = Color(0xFFE0E0E0),
        borderFocus = Color(0xFF009688),
        overlay = Color(0x80000000),
        aligned = Color(0xFF4CAF50),
        misaligned = Color(0xFFFF5722),
        segmentComplete = Color(0xFF4CAF50),
        segmentIncomplete = Color(0xFFE0E0E0)
    )
}
