/*
 * File: ColorScheme.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Global Color Scheme System
 * Similar to localization but for colors - allows easy switching between different color themes
 */

/**
 * Color scheme interface defining all colors used in the application
 */
interface AppColorScheme {
    // Primary Colors
    val primary: Color
    val primaryDark: Color
    val primaryLight: Color
    val onPrimary: Color
    
    // Secondary Colors
    val secondary: Color
    val secondaryDark: Color
    val secondaryLight: Color
    val onSecondary: Color
    
    // Background Colors
    val background: Color
    val backgroundSecondary: Color
    val surface: Color
    val surfaceVariant: Color
    val onBackground: Color
    val onSurface: Color
    
    // Text Colors
    val textPrimary: Color
    val textSecondary: Color
    val textDisabled: Color
    val textOnPrimary: Color
    val textOnSecondary: Color
    
    // Button Colors
    val buttonPrimary: Color
    val buttonSecondary: Color
    val buttonDisabled: Color
    val buttonTextPrimary: Color
    val buttonTextSecondary: Color
    val buttonTextDisabled: Color
    val buttonOutline: Color
    
    // Status Colors
    val success: Color
    val error: Color
    val warning: Color
    val info: Color
    val onSuccess: Color
    val onError: Color
    val onWarning: Color
    val onInfo: Color
    
    // Icon Colors
    val iconPrimary: Color
    val iconSecondary: Color
    val iconDisabled: Color
    val iconOnPrimary: Color
    val iconOnSecondary: Color
    
    // Overlay Colors
    val overlay: Color
    val overlayLight: Color
    val scrim: Color
    
    // Border Colors
    val border: Color
    val borderLight: Color
    val borderFocus: Color
    
    // Face Detection Colors
    val faceDetectionAligned: Color
    val faceDetectionMisaligned: Color
    val faceSegmentComplete: Color
    val faceSegmentIncomplete: Color
    
    // Document Detection Colors
    val documentDetectionAligned: Color
    val documentDetectionMisaligned: Color
    
    // Gradient Colors
    val gradientStart: Color
    val gradientEnd: Color
}

/**
 * Default Color Scheme - Matches iOS Standalone App (Light Theme)
 * Updated to match the exact iOS standalone application theme
 */
class DarkColorScheme : AppColorScheme {
    override val primary = Color(0xFF22354D) // iOS Bluegray900 - dark blue for buttons and accents
    override val primaryDark = Color(0xFF1A2B3D) // Darker version
    override val primaryLight = Color(0xFF3E517A) // Lighter version
    override val onPrimary = Color(0xFFFFFFFF) // White text on dark primary
    
    override val secondary = Color(0xFFF58220) // iOS Yellow900 - bright accent color
    override val secondaryDark = Color(0xFFE57100) // Darker orange
    override val secondaryLight = Color(0xFFFFB74D) // Lighter orange
    override val onSecondary = Color(0xFF22354D) // Dark text on bright secondary
    
    override val background = Color(0xFF22354D) // iOS Bluegray900 - primary background to match screenshot
    override val backgroundSecondary = Color(0xFF1A2332) // Slightly lighter dark background
    override val surface = Color(0xFF22354D) // iOS Bluegray900 - dark surface
    override val surfaceVariant = Color(0xFF162029) // Dark gray variant
    override val onBackground = Color(0xFFFFFFFF) // White text on dark background
    override val onSurface = Color(0xFFFFFFFF) // White text on dark surface
    
    override val textPrimary = Color(0xFFFFFFFF) // White text for dark theme
    override val textSecondary = Color(0xB3FFFFFF) // Semi-transparent white for secondary text
    override val textDisabled = Color(0x80FFFFFF) // More transparent white for disabled text
    override val textOnPrimary = Color(0xFFFFFFFF) // White text on dark primary
    override val textOnSecondary = Color(0xFF22354D) // Dark text on bright secondary
    
    override val buttonPrimary = Color(0xFFFFFFFF) // White background for primary buttons (like "Verify Now")
    override val buttonSecondary = Color(0xFFF58220) // Orange background for secondary buttons (like "Authenticate")
    override val buttonDisabled = Color(0xFFE9ECEF) // Light gray for disabled buttons
    override val buttonTextPrimary = Color(0xFFF58220) // Orange text on white buttons
    override val buttonTextSecondary = Color(0xFFFFFFFF) // White text on orange buttons
    override val buttonTextDisabled = Color(0xFFADB5BD) // Gray text on disabled buttons
    override val buttonOutline = Color(0xFF22354D) // Dark outline
    
    override val success = Color(0xFF4CAF50) // Standard green
    override val error = Color(0xFFD32F2F) // Standard red
    override val warning = Color(0xFFFF9800) // Standard orange
    override val info = Color(0xFF2196F3) // Standard blue
    override val onSuccess = Color(0xFFFFFFFF)
    override val onError = Color(0xFFFFFFFF)
    override val onWarning = Color(0xFFFFFFFF)
    override val onInfo = Color(0xFFFFFFFF)
    
    override val iconPrimary = Color(0xFF22354D) // Dark icons on light background
    override val iconSecondary = Color(0xFF6C757D) // Medium gray for secondary icons
    override val iconDisabled = Color(0xFFADB5BD) // Light gray for disabled icons
    override val iconOnPrimary = Color(0xFFFFFFFF) // White icons on dark primary
    override val iconOnSecondary = Color(0xFF22354D) // Dark icons on bright secondary
    
    override val overlay = Color(0x80000000) // Semi-transparent black
    override val overlayLight = Color(0x40000000) // Light semi-transparent black
    override val scrim = Color(0xB3000000) // Darker semi-transparent black
    
    override val border = Color(0xFFDEE2E6) // Light gray border
    override val borderLight = Color(0xFFF8F9FA) // Very light border
    override val borderFocus = Color(0xFFF58220) // Orange focus border
    
    override val faceDetectionAligned = Color(0xFFF58220) // iOS Yellow900 for aligned face
    override val faceDetectionMisaligned = Color(0xFFD32F2F) // Red for misaligned
    override val faceSegmentComplete = Color(0xFF4CAF50) // Green for complete
    override val faceSegmentIncomplete = Color(0xFFD32F2F) // Red for incomplete
    
    override val documentDetectionAligned = Color(0xFFF58220) // iOS Yellow900 for aligned document
    override val documentDetectionMisaligned = Color(0xFFD32F2F) // Red for misaligned
    
    override val gradientStart = Color(0xFFF8F9FA) // Light gradient start
    override val gradientEnd = Color(0xFFFFFFFF) // White gradient end
}

/**
 * Light Color Scheme
 */
class LightColorScheme : AppColorScheme {
    override val primary = Color(0xFFF58220) // Yellow900
    override val primaryDark = Color(0xFFE57100)
    override val primaryLight = Color(0xFFFFB74D)
    override val onPrimary = Color(0xFFFFFFFF)
    
    override val secondary = Color(0xFF1976D2)
    override val secondaryDark = Color(0xFF1565C0)
    override val secondaryLight = Color(0xFF42A5F5)
    override val onSecondary = Color(0xFFFFFFFF)
    
    override val background = Color(0xFFFFFFFF)
    override val backgroundSecondary = Color(0xFFF5F5F5)
    override val surface = Color(0xFFFFFFFF)
    override val surfaceVariant = Color(0xFFF0F0F0)
    override val onBackground = Color(0xFF000000)
    override val onSurface = Color(0xFF000000)
    
    override val textPrimary = Color(0xFF000000)
    override val textSecondary = Color(0xFF757575)
    override val textDisabled = Color(0xFFBDBDBD)
    override val textOnPrimary = Color(0xFFFFFFFF)
    override val textOnSecondary = Color(0xFFFFFFFF)
    
    override val buttonPrimary = Color(0xFFF58220)
    override val buttonSecondary = Color(0xFFFFFFFF)
    override val buttonDisabled = Color(0xFFE0E0E0)
    override val buttonTextPrimary = Color(0xFFFFFFFF)
    override val buttonTextSecondary = Color(0xFFF58220)
    override val buttonTextDisabled = Color(0xFFBDBDBD)
    override val buttonOutline = Color(0xFFF58220)
    
    override val success = Color(0xFF4CAF50)
    override val error = Color(0xFFE53935)
    override val warning = Color(0xFFFF9800)
    override val info = Color(0xFF2196F3)
    override val onSuccess = Color(0xFFFFFFFF)
    override val onError = Color(0xFFFFFFFF)
    override val onWarning = Color(0xFFFFFFFF)
    override val onInfo = Color(0xFFFFFFFF)
    
    override val iconPrimary = Color(0xFF000000)
    override val iconSecondary = Color(0xFF757575)
    override val iconDisabled = Color(0xFFBDBDBD)
    override val iconOnPrimary = Color(0xFFFFFFFF)
    override val iconOnSecondary = Color(0xFFFFFFFF)
    
    override val overlay = Color(0x80000000)
    override val overlayLight = Color(0x40000000)
    override val scrim = Color(0xB3000000)
    
    override val border = Color(0xFFE0E0E0)
    override val borderLight = Color(0xFFF0F0F0)
    override val borderFocus = Color(0xFFF58220)
    
    override val faceDetectionAligned = Color(0xFF4CAF50)
    override val faceDetectionMisaligned = Color(0xFFE53935)
    override val faceSegmentComplete = Color(0xFF4CAF50)
    override val faceSegmentIncomplete = Color(0xFFE53935)
    
    override val documentDetectionAligned = Color(0xFF4CAF50)
    override val documentDetectionMisaligned = Color(0xFFE53935)
    
    override val gradientStart = Color(0xFF3F51B5)
    override val gradientEnd = Color(0xFF2196F3)
}

/**
 * Alternative Color Scheme (Example for different branding)
 */
class AlternativeColorScheme : AppColorScheme {
    override val primary = Color(0xFF6200EE) // Purple
    override val primaryDark = Color(0xFF3700B3)
    override val primaryLight = Color(0xFFBB86FC)
    override val onPrimary = Color(0xFFFFFFFF)
    
    override val secondary = Color(0xFF03DAC6) // Teal
    override val secondaryDark = Color(0xFF018786)
    override val secondaryLight = Color(0xFF66FFF9)
    override val onSecondary = Color(0xFF000000)
    
    override val background = Color(0xFF121212)
    override val backgroundSecondary = Color(0xFF1E1E1E)
    override val surface = Color(0xFF1E1E1E)
    override val surfaceVariant = Color(0xFF2D2D2D)
    override val onBackground = Color(0xFFFFFFFF)
    override val onSurface = Color(0xFFFFFFFF)
    
    override val textPrimary = Color(0xFFFFFFFF)
    override val textSecondary = Color(0xB3FFFFFF)
    override val textDisabled = Color(0x80FFFFFF)
    override val textOnPrimary = Color(0xFFFFFFFF)
    override val textOnSecondary = Color(0xFF000000)
    
    override val buttonPrimary = Color(0xFF6200EE)
    override val buttonSecondary = Color(0xFF03DAC6)
    override val buttonDisabled = Color(0xFF424242)
    override val buttonTextPrimary = Color(0xFFFFFFFF)
    override val buttonTextSecondary = Color(0xFF000000)
    override val buttonTextDisabled = Color(0x80FFFFFF)
    override val buttonOutline = Color(0xFF6200EE)
    
    override val success = Color(0xFF00C853)
    override val error = Color(0xFFD32F2F)
    override val warning = Color(0xFFFF6F00)
    override val info = Color(0xFF0277BD)
    override val onSuccess = Color(0xFFFFFFFF)
    override val onError = Color(0xFFFFFFFF)
    override val onWarning = Color(0xFFFFFFFF)
    override val onInfo = Color(0xFFFFFFFF)
    
    override val iconPrimary = Color(0xFFFFFFFF)
    override val iconSecondary = Color(0xB3FFFFFF)
    override val iconDisabled = Color(0x80FFFFFF)
    override val iconOnPrimary = Color(0xFFFFFFFF)
    override val iconOnSecondary = Color(0xFF000000)
    
    override val overlay = Color(0x80000000)
    override val overlayLight = Color(0x40000000)
    override val scrim = Color(0xB3000000)
    
    override val border = Color(0x28FFFFFF)
    override val borderLight = Color(0x19FFFFFF)
    override val borderFocus = Color(0xFF6200EE)
    
    override val faceDetectionAligned = Color(0xFF00C853)
    override val faceDetectionMisaligned = Color(0xFFD32F2F)
    override val faceSegmentComplete = Color(0xFF00C853)
    override val faceSegmentIncomplete = Color(0xFFD32F2F)
    
    override val documentDetectionAligned = Color(0xFF00C853)
    override val documentDetectionMisaligned = Color(0xFFD32F2F)
    
    override val gradientStart = Color(0xFF6200EE)
    override val gradientEnd = Color(0xFF03DAC6)
}
