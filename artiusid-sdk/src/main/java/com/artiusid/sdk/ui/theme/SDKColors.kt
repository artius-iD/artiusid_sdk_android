package com.artiusid.sdk.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * SDK Color definitions for theming
 * These colors match the standalone application's color scheme
 */
object SDKColors {
    
    // Primary Colors from standalone app
    val Yellow900 = Color(0xFFF57C00)
    val LightGreen900 = Color(0xFF33691E)
    val Gray900 = Color(0xFF212121)
    val Gray500 = Color(0xFF9E9E9E)
    val Bluegray900 = Color(0xFF263238)
    val Bluegray901 = Color(0xFF37474F)
    val Bluegray902 = Color(0xFF455A64)
    
    // White variants
    val WhiteA700 = Color(0xFFFFFFFF)
    
    // Success/Error colors
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFD32F2F)
    
    // Text colors
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
    
    // Background colors
    val BackgroundPrimary = Color(0xFFFAFAFA)
    val BackgroundSecondary = Color(0xFFFFFFFF)
}

/**
 * Face detection specific colors
 */
object AppFaceColors {
    val FaceOutlineGood = SDKColors.Success
    val FaceOutlineBad = SDKColors.Error
    val FaceOutlineNeutral = SDKColors.Warning
}

/**
 * General app colors
 */
object AppColors {
    val Primary = SDKColors.Yellow900
    val Secondary = SDKColors.Gray900
    val Accent = SDKColors.LightGreen900
    val Background = SDKColors.BackgroundPrimary
    val Surface = SDKColors.WhiteA700
    val OnPrimary = SDKColors.WhiteA700
    val OnSecondary = SDKColors.WhiteA700
    val OnBackground = SDKColors.TextPrimary
    val OnSurface = SDKColors.TextPrimary
}

/**
 * Overlay colors for camera views
 */
object AppOverlayColors {
    val OverlayBackground = Color(0x80000000)
    val OverlayBorder = SDKColors.WhiteA700
    val Overlay = Color(0x80000000)
    val OverlayLight = Color(0x40000000)
}

/**
 * Document scanning colors
 */
object AppDocumentColors {
    val DocumentOutlineGood = SDKColors.Success
    val DocumentOutlineBad = SDKColors.Error
    val DocumentOutlineNeutral = SDKColors.Warning
}

// Expose individual colors for backward compatibility
val Yellow900 = SDKColors.Yellow900
val LightGreen900 = SDKColors.LightGreen900
val Gray900 = SDKColors.Gray900
val Gray500 = SDKColors.Gray500
val Bluegray900 = SDKColors.Bluegray900
val Bluegray901 = SDKColors.Bluegray901
val Bluegray902 = SDKColors.Bluegray902
val WhiteA700 = SDKColors.WhiteA700
val Success = SDKColors.Success
val Warning = SDKColors.Warning
val Error = SDKColors.Error
val TextPrimary = SDKColors.TextPrimary
val TextSecondary = SDKColors.TextSecondary
val Primary = SDKColors.Yellow900