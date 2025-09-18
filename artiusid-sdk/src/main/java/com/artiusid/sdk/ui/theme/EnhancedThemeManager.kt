/*
 * File: EnhancedThemeManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.models.EnhancedSDKThemeConfiguration
import com.artiusid.sdk.models.SDKTypography
import com.artiusid.sdk.models.SDKColorScheme
import com.artiusid.sdk.models.SDKIconTheme
import com.artiusid.sdk.models.SDKTextContent
import com.artiusid.sdk.models.SDKComponentStyling
import com.artiusid.sdk.models.SDKLayoutConfig
import com.artiusid.sdk.R

/**
 * Enhanced Theme Manager for comprehensive SDK theming
 * Converts theme configuration to Compose theme objects
 */
object EnhancedThemeManager {
    
    private var currentThemeConfig: EnhancedSDKThemeConfiguration? = null
    
    /**
     * Set the current theme configuration
     */
    fun setThemeConfiguration(config: EnhancedSDKThemeConfiguration) {
        currentThemeConfig = config
    }
    
    /**
     * Get the current theme configuration
     */
    fun getCurrentThemeConfig(): EnhancedSDKThemeConfiguration {
        return currentThemeConfig ?: EnhancedSDKThemeConfiguration()
    }
    
    /**
     * Convert SDK color scheme to Material3 ColorScheme
     */
    fun createColorScheme(colorConfig: SDKColorScheme): ColorScheme {
        return ColorScheme(
            primary = Color(android.graphics.Color.parseColor(colorConfig.primaryColorHex)),
            onPrimary = Color(android.graphics.Color.parseColor(colorConfig.onPrimaryColorHex)),
            primaryContainer = Color(android.graphics.Color.parseColor(colorConfig.primaryContainerColorHex)),
            onPrimaryContainer = Color(android.graphics.Color.parseColor(colorConfig.onPrimaryContainerColorHex)),
            
            secondary = Color(android.graphics.Color.parseColor(colorConfig.secondaryColorHex)),
            onSecondary = Color(android.graphics.Color.parseColor(colorConfig.onSecondaryColorHex)),
            secondaryContainer = Color(android.graphics.Color.parseColor(colorConfig.secondaryContainerColorHex)),
            onSecondaryContainer = Color(android.graphics.Color.parseColor(colorConfig.onSecondaryContainerColorHex)),
            
            tertiary = Color(android.graphics.Color.parseColor(colorConfig.secondaryColorHex)), // Use secondary as tertiary
            onTertiary = Color(android.graphics.Color.parseColor(colorConfig.onSecondaryColorHex)),
            tertiaryContainer = Color(android.graphics.Color.parseColor(colorConfig.secondaryContainerColorHex)),
            onTertiaryContainer = Color(android.graphics.Color.parseColor(colorConfig.onSecondaryContainerColorHex)),
            
            background = Color(android.graphics.Color.parseColor(colorConfig.backgroundColorHex)),
            onBackground = Color(android.graphics.Color.parseColor(colorConfig.onBackgroundColorHex)),
            surface = Color(android.graphics.Color.parseColor(colorConfig.surfaceColorHex)),
            onSurface = Color(android.graphics.Color.parseColor(colorConfig.onSurfaceColorHex)),
            surfaceVariant = Color(android.graphics.Color.parseColor(colorConfig.surfaceVariantColorHex)),
            onSurfaceVariant = Color(android.graphics.Color.parseColor(colorConfig.onSurfaceVariantColorHex)),
            
            error = Color(android.graphics.Color.parseColor(colorConfig.errorColorHex)),
            onError = Color(android.graphics.Color.parseColor(colorConfig.onErrorColorHex)),
            errorContainer = Color(android.graphics.Color.parseColor(colorConfig.errorColorHex)).copy(alpha = 0.12f),
            onErrorContainer = Color(android.graphics.Color.parseColor(colorConfig.onErrorColorHex)),
            
            outline = Color(android.graphics.Color.parseColor(colorConfig.outlineColorHex)),
            outlineVariant = Color(android.graphics.Color.parseColor(colorConfig.outlineVariantColorHex)),
            scrim = Color(android.graphics.Color.parseColor(colorConfig.scrimColorHex)),
            
            // Surface tonal variations
            surfaceTint = Color(android.graphics.Color.parseColor(colorConfig.primaryColorHex)),
            inverseSurface = Color(android.graphics.Color.parseColor(colorConfig.onSurfaceColorHex)),
            inverseOnSurface = Color(android.graphics.Color.parseColor(colorConfig.surfaceColorHex)),
            inversePrimary = Color(android.graphics.Color.parseColor(colorConfig.onPrimaryColorHex))
        )
    }
    
    /**
     * Convert SDK typography to Material3 Typography
     */
    fun createTypography(context: Context, typographyConfig: SDKTypography): Typography {
        val fontFamily = createFontFamily(context, typographyConfig)
        
        return Typography(
            headlineLarge = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.headlineLarge.sp,
                fontWeight = parseWeight(typographyConfig.headlineWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.headlineLarge * typographyConfig.lineHeight).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.headlineMedium.sp,
                fontWeight = parseWeight(typographyConfig.headlineWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.headlineMedium * typographyConfig.lineHeight).sp
            ),
            headlineSmall = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.headlineSmall.sp,
                fontWeight = parseWeight(typographyConfig.headlineWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.headlineSmall * typographyConfig.lineHeight).sp
            ),
            titleLarge = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.titleLarge.sp,
                fontWeight = parseWeight(typographyConfig.titleWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.titleLarge * typographyConfig.lineHeight).sp
            ),
            titleMedium = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.titleMedium.sp,
                fontWeight = parseWeight(typographyConfig.titleWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.titleMedium * typographyConfig.lineHeight).sp
            ),
            titleSmall = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.titleSmall.sp,
                fontWeight = parseWeight(typographyConfig.titleWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.titleSmall * typographyConfig.lineHeight).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.bodyLarge.sp,
                fontWeight = parseWeight(typographyConfig.bodyWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.bodyLarge * typographyConfig.lineHeight).sp
            ),
            bodyMedium = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.bodyMedium.sp,
                fontWeight = parseWeight(typographyConfig.bodyWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.bodyMedium * typographyConfig.lineHeight).sp
            ),
            bodySmall = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.bodySmall.sp,
                fontWeight = parseWeight(typographyConfig.bodyWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.bodySmall * typographyConfig.lineHeight).sp
            ),
            labelLarge = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.labelLarge.sp,
                fontWeight = parseWeight(typographyConfig.labelWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.labelLarge * typographyConfig.lineHeight).sp
            ),
            labelMedium = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.labelMedium.sp,
                fontWeight = parseWeight(typographyConfig.labelWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.labelMedium * typographyConfig.lineHeight).sp
            ),
            labelSmall = TextStyle(
                fontFamily = fontFamily,
                fontSize = typographyConfig.labelSmall.sp,
                fontWeight = parseWeight(typographyConfig.labelWeight),
                letterSpacing = typographyConfig.letterSpacing.sp,
                lineHeight = (typographyConfig.labelSmall * typographyConfig.lineHeight).sp
            )
        )
    }
    
    /**
     * Create FontFamily from configuration
     */
    private fun createFontFamily(context: Context, typographyConfig: SDKTypography): FontFamily {
        return when (typographyConfig.fontFamily.lowercase()) {
            "default" -> FontFamily.Default
            "serif" -> FontFamily.Serif
            "sans-serif" -> FontFamily.SansSerif
            "monospace" -> FontFamily.Monospace
            "cursive" -> FontFamily.Cursive
            else -> {
                // Try to load custom font
                try {
                    val prefix = typographyConfig.customFontResourcePrefix ?: typographyConfig.fontFamily
                    FontFamily(
                        Font(
                            resId = context.resources.getIdentifier(
                                "${prefix}_regular", 
                                "font", 
                                context.packageName
                            ),
                            weight = FontWeight.Normal
                        ),
                        Font(
                            resId = context.resources.getIdentifier(
                                "${prefix}_bold", 
                                "font", 
                                context.packageName
                            ),
                            weight = FontWeight.Bold
                        ),
                        Font(
                            resId = context.resources.getIdentifier(
                                "${prefix}_light", 
                                "font", 
                                context.packageName
                            ),
                            weight = FontWeight.Light
                        ),
                        Font(
                            resId = context.resources.getIdentifier(
                                "${prefix}_medium", 
                                "font", 
                                context.packageName
                            ),
                            weight = FontWeight.Medium
                        )
                    )
                } catch (e: Exception) {
                    // Fallback to default if custom font loading fails
                    FontFamily.Default
                }
            }
        }
    }
    
    /**
     * Parse font weight string to FontWeight
     */
    private fun parseWeight(weight: String): FontWeight {
        return when (weight.lowercase()) {
            "light" -> FontWeight.Light
            "normal" -> FontWeight.Normal
            "medium" -> FontWeight.Medium
            "bold" -> FontWeight.Bold
            "black" -> FontWeight.Black
            else -> FontWeight.Normal
        }
    }
    
    /**
     * Get icon resource ID from theme configuration
     */
    fun getIconResource(context: Context, iconName: String, iconTheme: SDKIconTheme): Int {
        // Check for custom icon mapping first
        val customIconName = iconTheme.customIcons[iconName]
        if (customIconName != null) {
            val resourceId = context.resources.getIdentifier(
                customIconName, 
                "drawable", 
                context.packageName
            )
            if (resourceId != 0) return resourceId
        }
        
        // Check for themed icon with prefix
        val prefix = iconTheme.customIconResourcePrefix
        if (prefix != null) {
            val themedIconName = "${prefix}_${iconName}"
            val resourceId = context.resources.getIdentifier(
                themedIconName, 
                "drawable", 
                context.packageName
            )
            if (resourceId != 0) return resourceId
        }
        
        // Fallback to default SDK icons
        return getDefaultIconResource(iconName)
    }
    
    /**
     * Get default SDK icon resource
     */
    private fun getDefaultIconResource(iconName: String): Int {
        return when (iconName) {
            "camera" -> R.drawable.camera_button_icon
            "face" -> R.drawable.scan_face_icon
            "document" -> R.drawable.doc_scan_icon
            "passport" -> R.drawable.passport_icon
            "nfc" -> R.drawable.passport_icon // Use passport icon for NFC
            "check" -> R.drawable.done_icon
            "error" -> R.drawable.error_icon
            "warning" -> R.drawable.informational_icon
            "info" -> R.drawable.informational_icon
            "back" -> R.drawable.back_button_icon
            "close" -> R.drawable.back_button_icon // Use back icon for close
            "refresh" -> R.drawable.focus_icon // Use focus icon for refresh
            "success" -> R.drawable.done_icon
            "failed" -> R.drawable.error_icon
            else -> R.drawable.informational_icon // Default fallback
        }
    }
}

/**
 * Composition Locals for theme access
 */
val LocalSDKTheme = staticCompositionLocalOf { EnhancedSDKThemeConfiguration() }
val LocalSDKTextContent = staticCompositionLocalOf { SDKTextContent() }
val LocalSDKIconTheme = staticCompositionLocalOf { SDKIconTheme() }
val LocalSDKComponentStyling = staticCompositionLocalOf { SDKComponentStyling() }
val LocalSDKLayoutConfig = staticCompositionLocalOf { SDKLayoutConfig() }

/**
 * Enhanced SDK Theme Provider
 */
@Composable
fun EnhancedSDKTheme(
    themeConfig: EnhancedSDKThemeConfiguration,
    content: @Composable () -> Unit
) {
    // Set the theme configuration in the manager
    EnhancedThemeManager.setThemeConfiguration(themeConfig)
    
    // Provide theme values through composition locals
    CompositionLocalProvider(
        LocalSDKTheme provides themeConfig,
        LocalSDKTextContent provides themeConfig.textContent,
        LocalSDKIconTheme provides themeConfig.iconTheme,
        LocalSDKComponentStyling provides themeConfig.componentStyling,
        LocalSDKLayoutConfig provides themeConfig.layoutConfig
    ) {
        content()
    }
}
