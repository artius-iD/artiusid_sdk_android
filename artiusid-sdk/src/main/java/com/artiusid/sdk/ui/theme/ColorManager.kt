/*
 * File: ColorManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.models.SDKColorScheme

/**
 * Global Color Manager
 * Provides centralized access to the current color scheme throughout the app
 */

/**
 * Available color scheme types
 */
enum class ColorSchemeType {
    DARK,
    LIGHT,
    ALTERNATIVE
}

/**
 * Color Manager object that handles color scheme switching
 */
object ColorManager {
    private var currentSchemeType = ColorSchemeType.DARK
    private var currentScheme: AppColorScheme = DarkColorScheme()
    private var enhancedTheme: EnhancedSDKThemeConfiguration? = null
    private var isUsingEnhancedTheme = false
    
    /**
     * Set enhanced theme configuration (takes priority over legacy color schemes)
     */
    fun setEnhancedTheme(theme: EnhancedSDKThemeConfiguration) {
        enhancedTheme = theme
        isUsingEnhancedTheme = true
        
        // Convert enhanced theme to AppColorScheme for backward compatibility
        currentScheme = createAppColorSchemeFromEnhanced(theme)
        
        android.util.Log.d("ColorManager", "🎨 Enhanced theme applied: ${theme.brandName}")
    }
    
    /**
     * Clear enhanced theme and revert to legacy color schemes
     */
    fun clearEnhancedTheme() {
        enhancedTheme = null
        isUsingEnhancedTheme = false
        
        // Revert to legacy scheme
        setColorScheme(currentSchemeType)
        
        android.util.Log.d("ColorManager", "🎨 Reverted to legacy color scheme: $currentSchemeType")
    }
    
    /**
     * Get the current enhanced theme (if set)
     */
    fun getCurrentEnhancedTheme(): EnhancedSDKThemeConfiguration? = enhancedTheme
    
    /**
     * Check if using enhanced theming
     */
    fun isUsingEnhancedTheming(): Boolean = isUsingEnhancedTheme
    
    /**
     * Get the current color scheme
     */
    fun getCurrentScheme(): AppColorScheme = currentScheme
    
    /**
     * Get the current scheme type
     */
    fun getCurrentSchemeType(): ColorSchemeType = currentSchemeType
    
    /**
     * Switch to a different color scheme (only works if not using enhanced theming)
     */
    fun setColorScheme(schemeType: ColorSchemeType) {
        if (isUsingEnhancedTheme) {
            android.util.Log.w("ColorManager", "⚠️ Cannot set legacy color scheme while using enhanced theming")
            return
        }
        
        currentSchemeType = schemeType
        currentScheme = when (schemeType) {
            ColorSchemeType.DARK -> DarkColorScheme()
            ColorSchemeType.LIGHT -> LightColorScheme()
            ColorSchemeType.ALTERNATIVE -> AlternativeColorScheme()
        }
    }
    
    /**
     * Get gradient brush for backgrounds
     */
    fun getGradientBrush(): Brush {
        return if (isUsingEnhancedTheme && enhancedTheme != null) {
            // Use enhanced theme colors for gradient
            Brush.verticalGradient(
                colors = listOf(
                    Color(android.graphics.Color.parseColor(enhancedTheme!!.colorScheme.backgroundColorHex)),
                    Color(android.graphics.Color.parseColor(enhancedTheme!!.colorScheme.surfaceColorHex))
                )
            )
        } else {
            // Use legacy gradient colors
            Brush.verticalGradient(
                colors = listOf(
                    currentScheme.gradientStart,
                    currentScheme.gradientEnd
                )
            )
        }
    }
    
    /**
     * Convert enhanced theme to AppColorScheme for backward compatibility
     */
    private fun createAppColorSchemeFromEnhanced(theme: EnhancedSDKThemeConfiguration): AppColorScheme {
        return object : AppColorScheme {
            override val primary = Color(android.graphics.Color.parseColor(theme.colorScheme.primaryColorHex))
            override val primaryDark = Color(android.graphics.Color.parseColor(theme.colorScheme.primaryColorHex)).copy(alpha = 0.8f)
            override val primaryLight = Color(android.graphics.Color.parseColor(theme.colorScheme.primaryColorHex)).copy(alpha = 0.6f)
            override val onPrimary = Color(android.graphics.Color.parseColor(theme.colorScheme.onPrimaryColorHex))
            
            override val secondary = Color(android.graphics.Color.parseColor(theme.colorScheme.secondaryColorHex))
            override val secondaryDark = Color(android.graphics.Color.parseColor(theme.colorScheme.secondaryColorHex)).copy(alpha = 0.8f)
            override val secondaryLight = Color(android.graphics.Color.parseColor(theme.colorScheme.secondaryColorHex)).copy(alpha = 0.6f)
            override val onSecondary = Color(android.graphics.Color.parseColor(theme.colorScheme.onSecondaryColorHex))
            
            override val background = Color(android.graphics.Color.parseColor(theme.colorScheme.backgroundColorHex))
            override val backgroundSecondary = Color(android.graphics.Color.parseColor(theme.colorScheme.surfaceVariantColorHex))
            override val surface = Color(android.graphics.Color.parseColor(theme.colorScheme.surfaceColorHex))
            override val surfaceVariant = Color(android.graphics.Color.parseColor(theme.colorScheme.surfaceVariantColorHex))
            override val onBackground = Color(android.graphics.Color.parseColor(theme.colorScheme.onBackgroundColorHex))
            override val onSurface = Color(android.graphics.Color.parseColor(theme.colorScheme.onSurfaceColorHex))
            
            override val textPrimary = Color(android.graphics.Color.parseColor(theme.colorScheme.onBackgroundColorHex))
            override val textSecondary = Color(android.graphics.Color.parseColor(theme.colorScheme.onSurfaceVariantColorHex))
            override val textDisabled = Color(android.graphics.Color.parseColor(theme.colorScheme.onSurfaceVariantColorHex)).copy(alpha = 0.6f)
            override val textOnPrimary = Color(android.graphics.Color.parseColor(theme.colorScheme.onPrimaryColorHex))
            override val textOnSecondary = Color(android.graphics.Color.parseColor(theme.colorScheme.onSecondaryColorHex))
            
            override val buttonPrimary = Color(android.graphics.Color.parseColor(theme.colorScheme.primaryButtonColorHex))
            override val buttonSecondary = Color(android.graphics.Color.parseColor(theme.colorScheme.secondaryButtonColorHex))
            override val buttonDisabled = Color(android.graphics.Color.parseColor(theme.colorScheme.disabledButtonColorHex))
            override val buttonTextPrimary = Color(android.graphics.Color.parseColor(theme.colorScheme.primaryButtonTextColorHex))
            override val buttonTextSecondary = Color(android.graphics.Color.parseColor(theme.colorScheme.secondaryButtonTextColorHex))
            override val buttonTextDisabled = Color(android.graphics.Color.parseColor(theme.colorScheme.disabledButtonTextColorHex))
            override val buttonOutline = Color(android.graphics.Color.parseColor(theme.colorScheme.outlineColorHex))
            
            override val success = Color(android.graphics.Color.parseColor(theme.colorScheme.successColorHex))
            override val error = Color(android.graphics.Color.parseColor(theme.colorScheme.errorColorHex))
            override val warning = Color(android.graphics.Color.parseColor(theme.colorScheme.warningColorHex))
            override val info = Color(android.graphics.Color.parseColor(theme.colorScheme.infoColorHex))
            override val onSuccess = Color(android.graphics.Color.parseColor(theme.colorScheme.onSuccessColorHex))
            override val onError = Color(android.graphics.Color.parseColor(theme.colorScheme.onErrorColorHex))
            override val onWarning = Color(android.graphics.Color.parseColor(theme.colorScheme.onWarningColorHex))
            override val onInfo = Color(android.graphics.Color.parseColor(theme.colorScheme.onInfoColorHex))
            
            override val iconPrimary = Color(android.graphics.Color.parseColor(theme.iconTheme.primaryIconColorHex))
            override val iconSecondary = Color(android.graphics.Color.parseColor(theme.iconTheme.secondaryIconColorHex))
            override val iconDisabled = Color(android.graphics.Color.parseColor(theme.iconTheme.disabledIconColorHex))
            override val iconOnPrimary = Color(android.graphics.Color.parseColor(theme.colorScheme.onPrimaryColorHex))
            override val iconOnSecondary = Color(android.graphics.Color.parseColor(theme.colorScheme.onSecondaryColorHex))
            
            override val overlay = Color(android.graphics.Color.parseColor(theme.colorScheme.overlayColorHex))
            override val overlayLight = Color(android.graphics.Color.parseColor(theme.colorScheme.overlayColorHex)).copy(alpha = 0.4f)
            override val scrim = Color(android.graphics.Color.parseColor(theme.colorScheme.scrimColorHex))
            
            override val border = Color(android.graphics.Color.parseColor(theme.colorScheme.outlineColorHex))
            override val borderLight = Color(android.graphics.Color.parseColor(theme.colorScheme.outlineVariantColorHex))
            override val borderFocus = Color(android.graphics.Color.parseColor(theme.colorScheme.primaryColorHex))
            
            override val faceDetectionAligned = Color(android.graphics.Color.parseColor(theme.colorScheme.faceDetectionOverlayColorHex))
            override val faceDetectionMisaligned = Color(android.graphics.Color.parseColor(theme.colorScheme.errorColorHex))
            override val faceSegmentComplete = Color(android.graphics.Color.parseColor(theme.colorScheme.successColorHex))
            override val faceSegmentIncomplete = Color(android.graphics.Color.parseColor(theme.colorScheme.errorColorHex))
            
            override val documentDetectionAligned = Color(android.graphics.Color.parseColor(theme.colorScheme.documentScanOverlayColorHex))
            override val documentDetectionMisaligned = Color(android.graphics.Color.parseColor(theme.colorScheme.errorColorHex))
            
            override val gradientStart = Color(android.graphics.Color.parseColor(theme.colorScheme.backgroundColorHex))
            override val gradientEnd = Color(android.graphics.Color.parseColor(theme.colorScheme.surfaceColorHex))
        }
    }
    
    /**
     * Get all available color scheme types
     */
    fun getAvailableSchemes(): List<ColorSchemeType> {
        return ColorSchemeType.values().toList()
    }
}

/**
 * Composition Local for providing color scheme throughout the app
 */
val LocalAppColorScheme = staticCompositionLocalOf<AppColorScheme> {
    DarkColorScheme()
}

/**
 * Composable function to provide color scheme to the composition tree
 */
@Composable
fun ProvideAppColorScheme(
    colorScheme: AppColorScheme = ColorManager.getCurrentScheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppColorScheme provides colorScheme,
        content = content
    )
}

/**
 * Extension property to easily access app colors from any Composable
 */
val AppColors: AppColorScheme
    @Composable
    get() = LocalAppColorScheme.current
