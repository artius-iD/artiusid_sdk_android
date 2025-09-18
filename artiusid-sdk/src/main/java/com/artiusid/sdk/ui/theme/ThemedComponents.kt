/*
 * File: ThemedComponents.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Themed version of GradientBackground that uses enhanced theming
 * This replaces the hardcoded GradientBackground component
 */
@Composable
fun ThemedGradientBackground(content: @Composable () -> Unit) {
    // Use ColorManager's enhanced theming-aware gradient
    val gradientBrush = ColorManager.getGradientBrush()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        content()
    }
}

/**
 * Themed version of face detection overlay colors
 */
object ThemedFaceDetection {
    @Composable
    fun getAlignedColor(): Color {
        val appColors = AppColors
        return appColors.faceDetectionAligned
    }
    
    @Composable
    fun getMisalignedColor(): Color {
        val appColors = AppColors
        return appColors.faceDetectionMisaligned
    }
    
    @Composable
    fun getCompleteColor(): Color {
        val appColors = AppColors
        return appColors.faceSegmentComplete
    }
    
    @Composable
    fun getIncompleteColor(): Color {
        val appColors = AppColors
        return appColors.faceSegmentIncomplete
    }
}

/**
 * Themed version of document detection overlay colors
 */
object ThemedDocumentDetection {
    @Composable
    fun getAlignedColor(): Color {
        val appColors = AppColors
        return appColors.documentDetectionAligned
    }
    
    @Composable
    fun getMisalignedColor(): Color {
        val appColors = AppColors
        return appColors.documentDetectionMisaligned
    }
}

/**
 * Themed status colors for various UI states
 */
object ThemedStatusColors {
    @Composable
    fun getSuccessColor(): Color {
        val appColors = AppColors
        return appColors.success
    }
    
    @Composable
    fun getErrorColor(): Color {
        val appColors = AppColors
        return appColors.error
    }
    
    @Composable
    fun getWarningColor(): Color {
        val appColors = AppColors
        return appColors.warning
    }
    
    @Composable
    fun getInfoColor(): Color {
        val appColors = AppColors
        return appColors.info
    }
    
    @Composable
    fun getProcessingColor(): Color {
        // Use enhanced theme processing color if available, otherwise use info color
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.colorScheme.processingColorHex))
            } else {
                AppColors.info
            }
        } else {
            AppColors.info
        }
    }
}

/**
 * Themed button colors that respect enhanced theming
 */
object ThemedButtonColors {
    @Composable
    fun getPrimaryButtonColor(): Color {
        val appColors = AppColors
        return appColors.buttonPrimary
    }
    
    @Composable
    fun getPrimaryButtonTextColor(): Color {
        val appColors = AppColors
        return appColors.buttonTextPrimary
    }
    
    @Composable
    fun getSecondaryButtonColor(): Color {
        val appColors = AppColors
        return appColors.buttonSecondary
    }
    
    @Composable
    fun getSecondaryButtonTextColor(): Color {
        val appColors = AppColors
        return appColors.buttonTextSecondary
    }
    
    @Composable
    fun getDisabledButtonColor(): Color {
        val appColors = AppColors
        return appColors.buttonDisabled
    }
    
    @Composable
    fun getDisabledButtonTextColor(): Color {
        val appColors = AppColors
        return appColors.buttonTextDisabled
    }
}

/**
 * Utility functions for color contrast calculations
 */
object ContrastUtils {
    /**
     * Determines if a background color is light (luminance > 0.5)
     */
    fun isLightBackground(backgroundColor: Color): Boolean {
        return backgroundColor.luminance() > 0.5f
    }
    
    /**
     * Gets appropriate text color for given background to ensure readability
     */
    fun getContrastingTextColor(backgroundColor: Color): Color {
        return if (isLightBackground(backgroundColor)) {
            Color.Black.copy(alpha = 0.87f) // Dark text on light background
        } else {
            Color.White.copy(alpha = 0.87f) // Light text on dark background
        }
    }
    
    /**
     * Gets appropriate secondary text color for given background
     */
    fun getContrastingSecondaryTextColor(backgroundColor: Color): Color {
        return if (isLightBackground(backgroundColor)) {
            Color.Black.copy(alpha = 0.60f) // Dark secondary text on light background
        } else {
            Color.White.copy(alpha = 0.60f) // Light secondary text on dark background
        }
    }
}

/**
 * Themed text colors that respect enhanced theming with automatic contrast
 */
object ThemedTextColors {
    @Composable
    fun getPrimaryTextColor(): Color {
        val backgroundColor = ColorManager.getCurrentScheme().background
        return ContrastUtils.getContrastingTextColor(backgroundColor)
    }
    
    @Composable
    fun getSecondaryTextColor(): Color {
        val backgroundColor = ColorManager.getCurrentScheme().background
        return ContrastUtils.getContrastingSecondaryTextColor(backgroundColor)
    }
    
    @Composable
    fun getDisabledTextColor(): Color {
        val appColors = AppColors
        return appColors.textDisabled
    }
    
    @Composable
    fun getOnPrimaryTextColor(): Color {
        val appColors = AppColors
        return appColors.textOnPrimary
    }
    
    @Composable
    fun getOnSecondaryTextColor(): Color {
        val appColors = AppColors
        return appColors.textOnSecondary
    }
}

/**
 * Comprehensive themed icon colors for all SDK icons
 */
object ThemedIconColors {
    // General Icon Colors
    @Composable
    fun getPrimaryIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.primaryIconColorHex))
            } else {
                ColorManager.getCurrentScheme().iconPrimary
            }
        } else {
            ColorManager.getCurrentScheme().iconPrimary
        }
    }
    
    @Composable
    fun getSecondaryIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.secondaryIconColorHex))
            } else {
                ColorManager.getCurrentScheme().iconSecondary
            }
        } else {
            ColorManager.getCurrentScheme().iconSecondary
        }
    }
    
    @Composable
    fun getAccentIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.accentIconColorHex))
            } else {
                ColorManager.getCurrentScheme().primary
            }
        } else {
            ColorManager.getCurrentScheme().primary
        }
    }
    
    // Navigation & UI Icons
    @Composable
    fun getNavigationIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.navigationIconColorHex))
            } else {
                getPrimaryIconColor()
            }
        } else {
            getPrimaryIconColor()
        }
    }
    
    @Composable
    fun getActionIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.actionIconColorHex))
            } else {
                getAccentIconColor()
            }
        } else {
            getAccentIconColor()
        }
    }
    
    // Instruction & Guide Icons
    @Composable
    fun getInstructionIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.instructionIconColorHex))
            } else {
                getAccentIconColor()
            }
        } else {
            getAccentIconColor()
        }
    }
    
    @Composable
    fun getWarningIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.warningIconColorHex))
            } else {
                ThemedStatusColors.getWarningColor()
            }
        } else {
            ThemedStatusColors.getWarningColor()
        }
    }
    
    @Composable
    fun getErrorIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.errorIconColorHex))
            } else {
                ThemedStatusColors.getErrorColor()
            }
        } else {
            ThemedStatusColors.getErrorColor()
        }
    }
    
    @Composable
    fun getSuccessIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.successIconColorHex))
            } else {
                ThemedStatusColors.getSuccessColor()
            }
        } else {
            ThemedStatusColors.getSuccessColor()
        }
    }
    
    // Document & Verification Icons
    @Composable
    fun getDocumentIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.documentIconColorHex))
            } else {
                getAccentIconColor()
            }
        } else {
            getAccentIconColor()
        }
    }
    
    @Composable
    fun getCameraIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.cameraIconColorHex))
            } else {
                getPrimaryIconColor()
            }
        } else {
            getPrimaryIconColor()
        }
    }
    
    @Composable
    fun getScanIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.scanIconColorHex))
            } else {
                getAccentIconColor()
            }
        } else {
            getAccentIconColor()
        }
    }
    
    // Biometric & Security Icons
    @Composable
    fun getBiometricIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.biometricIconColorHex))
            } else {
                getAccentIconColor()
            }
        } else {
            getAccentIconColor()
        }
    }
    
    @Composable
    fun getSecurityIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.securityIconColorHex))
            } else {
                getSuccessIconColor()
            }
        } else {
            getSuccessIconColor()
        }
    }
    
    @Composable
    fun getNfcIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.nfcIconColorHex))
            } else {
                getAccentIconColor()
            }
        } else {
            getAccentIconColor()
        }
    }
    
    // Status Icons
    @Composable
    fun getStatusActiveIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.statusActiveIconColorHex))
            } else {
                getSuccessIconColor()
            }
        } else {
            getSuccessIconColor()
        }
    }
    
    @Composable
    fun getStatusInactiveIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.statusInactiveIconColorHex))
            } else {
                getSecondaryIconColor()
            }
        } else {
            getSecondaryIconColor()
        }
    }
    
    @Composable
    fun getStatusProcessingIconColor(): Color {
        return if (ColorManager.isUsingEnhancedTheming()) {
            val enhancedTheme = ColorManager.getCurrentEnhancedTheme()
            if (enhancedTheme != null) {
                Color(android.graphics.Color.parseColor(enhancedTheme.iconTheme.statusProcessingIconColorHex))
            } else {
                getAccentIconColor()
            }
        } else {
            getAccentIconColor()
        }
    }
}

/**
 * Themed surface and background colors
 */
object ThemedSurfaceColors {
    @Composable
    fun getBackgroundColor(): Color {
        val appColors = AppColors
        return appColors.background
    }
    
    @Composable
    fun getSecondaryBackgroundColor(): Color {
        val appColors = AppColors
        return appColors.backgroundSecondary
    }
    
    @Composable
    fun getSurfaceColor(): Color {
        val appColors = AppColors
        return appColors.surface
    }
    
    @Composable
    fun getSurfaceVariantColor(): Color {
        val appColors = AppColors
        return appColors.surfaceVariant
    }
    
    @Composable
    fun getOnBackgroundColor(): Color {
        val appColors = AppColors
        return appColors.onBackground
    }
    
    @Composable
    fun getOnSurfaceColor(): Color {
        val appColors = AppColors
        return appColors.onSurface
    }
}
