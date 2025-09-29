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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import java.util.concurrent.CopyOnWriteArrayList
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
    private val _currentThemeState = mutableStateOf<EnhancedSDKThemeConfiguration?>(null)
    
    // Callback system for theme changes (works across activities)
    private val themeChangeListeners = CopyOnWriteArrayList<(EnhancedSDKThemeConfiguration?) -> Unit>()
    
    // Default artius.iD theme that matches iOS standalone app
    private val defaultArtiusIDTheme = createDefaultArtiusIDTheme()
    
    init {
        // Set default theme on initialization
        currentThemeConfig = defaultArtiusIDTheme
        _currentThemeState.value = defaultArtiusIDTheme
    }
    
    /**
     * Set the current theme configuration
     */
    fun setThemeConfiguration(config: EnhancedSDKThemeConfiguration) {
        currentThemeConfig = config
        _currentThemeState.value = config
        
        // Also update ColorManager to ensure gradient backgrounds use the new theme
        ColorManager.setEnhancedTheme(config)
        
        // Notify all listeners (including MainActivity)
        themeChangeListeners.forEach { listener ->
            try {
                listener(config)
            } catch (e: Exception) {
                android.util.Log.w("EnhancedThemeManager", "Theme listener failed: ${e.message}")
            }
        }
        
        android.util.Log.d("EnhancedThemeManager", "🎨 setThemeConfiguration called: ${config.brandName}")
        android.util.Log.d("EnhancedThemeManager", "🎨 Reactive state set: ${_currentThemeState.value?.brandName ?: "default"}")
        android.util.Log.d("EnhancedThemeManager", "🎨 Notified ${themeChangeListeners.size} listeners")
    }
    
    /**
     * Get the current theme configuration
     */
    fun getCurrentThemeConfig(): EnhancedSDKThemeConfiguration {
        val theme = currentThemeConfig ?: defaultArtiusIDTheme
        android.util.Log.d("EnhancedThemeManager", "🎨 getCurrentThemeConfig() returning: ${theme.brandName}")
        android.util.Log.d("EnhancedThemeManager", "🎨 currentThemeConfig is ${if (currentThemeConfig == null) "null" else "not null"}")
        return theme
    }
    
    /**
     * Get the current theme configuration as reactive State
     */
    fun getCurrentThemeState(): State<EnhancedSDKThemeConfiguration?> {
        return _currentThemeState
    }
    
    /**
     * Register a listener for theme changes (for cross-activity communication)
     */
    fun addThemeChangeListener(listener: (EnhancedSDKThemeConfiguration?) -> Unit) {
        themeChangeListeners.add(listener)
        android.util.Log.d("EnhancedThemeManager", "🎨 Theme listener added. Total: ${themeChangeListeners.size}")
    }
    
    /**
     * Unregister a theme change listener
     */
    fun removeThemeChangeListener(listener: (EnhancedSDKThemeConfiguration?) -> Unit) {
        themeChangeListeners.remove(listener)
        android.util.Log.d("EnhancedThemeManager", "🎨 Theme listener removed. Total: ${themeChangeListeners.size}")
    }
    
    /**
     * Update the current theme configuration
     */
    fun updateCurrentThemeConfig(config: EnhancedSDKThemeConfiguration?) {
        android.util.Log.d("EnhancedThemeManager", "🎨 updateCurrentThemeConfig called with: ${config?.brandName ?: "null"}")
        if (config != null) {
            android.util.Log.d("EnhancedThemeManager", "🎨 New theme background: ${config.colorScheme.backgroundColorHex}")
            android.util.Log.d("EnhancedThemeManager", "🎨 New theme primary button: ${config.colorScheme.primaryButtonColorHex}")
        }
        
        currentThemeConfig = config
        _currentThemeState.value = config ?: defaultArtiusIDTheme
        
        // Also update ColorManager to ensure gradient backgrounds use the new theme
        if (config != null) {
            ColorManager.setEnhancedTheme(config)
        }
        
        // Notify all listeners (including MainActivity)
        themeChangeListeners.forEach { listener ->
            try {
                listener(config)
            } catch (e: Exception) {
                android.util.Log.w("EnhancedThemeManager", "Theme listener failed: ${e.message}")
            }
        }
        
        android.util.Log.d("EnhancedThemeManager", "🎨 Theme updated: ${config?.brandName ?: "default"}")
        android.util.Log.d("EnhancedThemeManager", "🎨 Reactive state updated: ${_currentThemeState.value?.brandName ?: "default"}")
        android.util.Log.d("EnhancedThemeManager", "🎨 Notified ${themeChangeListeners.size} listeners")
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
val LocalSDKTheme = staticCompositionLocalOf { EnhancedThemeManager.getCurrentThemeConfig() }
val LocalSDKTextContent = staticCompositionLocalOf { EnhancedThemeManager.getCurrentThemeConfig().textContent }
val LocalSDKIconTheme = staticCompositionLocalOf { EnhancedThemeManager.getCurrentThemeConfig().iconTheme }
val LocalSDKComponentStyling = staticCompositionLocalOf { EnhancedThemeManager.getCurrentThemeConfig().componentStyling }
val LocalSDKLayoutConfig = staticCompositionLocalOf { EnhancedThemeManager.getCurrentThemeConfig().layoutConfig }

/**
 * Enhanced SDK Theme Provider
 */
@Composable
fun EnhancedSDKTheme(
    themeConfig: EnhancedSDKThemeConfiguration,
    content: @Composable () -> Unit
) {
    // Log the theme being applied for debugging
    android.util.Log.d("EnhancedSDKTheme", "🎨 Applying theme: ${themeConfig.brandName}")
    
    // Create AppColorScheme from the theme configuration
    val appColorScheme = createAppColorSchemeFromSDKTheme(themeConfig.colorScheme)
    
    // Provide theme values through composition locals
    // NOTE: We don't call setThemeConfiguration here to avoid overriding the globally set theme
    // The theme should be set by the SDK initialization or theme selection
    CompositionLocalProvider(
        LocalSDKTheme provides themeConfig,
        LocalSDKTextContent provides themeConfig.textContent,
        LocalSDKIconTheme provides themeConfig.iconTheme,
        LocalSDKComponentStyling provides themeConfig.componentStyling,
        LocalSDKLayoutConfig provides themeConfig.layoutConfig,
        LocalAppColorScheme provides appColorScheme
    ) {
        content()
    }
}

/**
 * Create AppColorScheme from SDK theme configuration
 */
private fun createAppColorSchemeFromSDKTheme(colorScheme: SDKColorScheme): AppColorScheme {
    // Helper function to safely parse colors with fallback
    fun safeParseColor(hexColor: String, fallback: Long = 0xFF000000): Color {
        return try {
            Color(android.graphics.Color.parseColor(hexColor))
        } catch (e: Exception) {
            android.util.Log.w("EnhancedThemeManager", "Failed to parse color: $hexColor, using fallback")
            Color(fallback)
        }
    }
    
    return object : AppColorScheme {
        override val primary: Color = safeParseColor(colorScheme.primaryColorHex, 0xFF22354D)
        override val primaryDark: Color = safeParseColor(colorScheme.primaryColorHex, 0xFF1A2B3D)
        override val primaryLight: Color = safeParseColor(colorScheme.primaryColorHex, 0xFF3E517A)
        override val onPrimary: Color = safeParseColor(colorScheme.onPrimaryColorHex, 0xFFFFFFFF)
        
        override val secondary: Color = safeParseColor(colorScheme.secondaryColorHex, 0xFFF58220)
        override val secondaryDark: Color = safeParseColor(colorScheme.secondaryColorHex, 0xFFE57100)
        override val secondaryLight: Color = safeParseColor(colorScheme.secondaryColorHex, 0xFFFFB74D)
        override val onSecondary: Color = safeParseColor(colorScheme.onSecondaryColorHex, 0xFFFFFFFF)
        
        override val background: Color = safeParseColor(colorScheme.backgroundColorHex, 0xFF22354D)
        override val backgroundSecondary: Color = safeParseColor(colorScheme.backgroundColorHex, 0xFF1A2332)
        override val surface: Color = safeParseColor(colorScheme.surfaceColorHex, 0xFF22354D)
        override val surfaceVariant: Color = safeParseColor(colorScheme.surfaceColorHex, 0xFF162029)
        override val onBackground: Color = safeParseColor(colorScheme.onBackgroundColorHex, 0xFFFFFFFF)
        override val onSurface: Color = safeParseColor(colorScheme.onSurfaceColorHex, 0xFFFFFFFF)
        
        override val textPrimary: Color = safeParseColor(colorScheme.onBackgroundColorHex, 0xFFFFFFFF)
        override val textSecondary: Color = safeParseColor(colorScheme.onBackgroundColorHex, 0xFFFFFFFF).copy(alpha = 0.7f)
        override val textDisabled: Color = safeParseColor(colorScheme.onBackgroundColorHex, 0xFFFFFFFF).copy(alpha = 0.5f)
        override val textOnPrimary: Color = safeParseColor(colorScheme.onPrimaryColorHex, 0xFFFFFFFF)
        override val textOnSecondary: Color = safeParseColor(colorScheme.onSecondaryColorHex, 0xFFFFFFFF)
        
        override val buttonPrimary: Color = safeParseColor(colorScheme.primaryButtonColorHex, 0xFFF58220)
        override val buttonSecondary: Color = safeParseColor(colorScheme.secondaryButtonColorHex, 0xFFF58220)
        override val buttonDisabled: Color = Color(0xFFE0E0E0)
        override val buttonTextPrimary: Color = safeParseColor(colorScheme.primaryButtonTextColorHex, 0xFFFFFFFF)
        override val buttonTextSecondary: Color = safeParseColor(colorScheme.secondaryButtonTextColorHex, 0xFFFFFFFF)
        override val buttonTextDisabled: Color = Color(0xFFBDBDBD)
        override val buttonOutline: Color = safeParseColor(colorScheme.primaryColorHex, 0xFF22354D)
        
        override val success: Color = safeParseColor(colorScheme.successColorHex, 0xFF4CAF50)
        override val error: Color = safeParseColor(colorScheme.errorColorHex, 0xFFD32F2F)
        override val warning: Color = safeParseColor(colorScheme.warningColorHex, 0xFFFF9800)
        override val info: Color = safeParseColor(colorScheme.primaryColorHex, 0xFF2196F3)
        override val onSuccess: Color = Color(0xFFFFFFFF)
        override val onError: Color = Color(0xFFFFFFFF)
        override val onWarning: Color = Color(0xFFFFFFFF)
        override val onInfo: Color = Color(0xFFFFFFFF)
        
        override val iconPrimary: Color = safeParseColor(colorScheme.onBackgroundColorHex, 0xFFFFFFFF)
        override val iconSecondary: Color = safeParseColor(colorScheme.secondaryColorHex, 0xFFF58220)
        override val iconDisabled: Color = safeParseColor(colorScheme.onBackgroundColorHex, 0xFFFFFFFF).copy(alpha = 0.5f)
        override val iconOnPrimary: Color = safeParseColor(colorScheme.onPrimaryColorHex, 0xFFFFFFFF)
        override val iconOnSecondary: Color = safeParseColor(colorScheme.onSecondaryColorHex, 0xFFFFFFFF)
        
        override val overlay: Color = Color(0x80000000)
        override val overlayLight: Color = Color(0x40000000)
        override val scrim: Color = Color(0xB3000000)
        
        override val border: Color = safeParseColor(colorScheme.onBackgroundColorHex, 0xFFFFFFFF).copy(alpha = 0.2f)
        override val borderLight: Color = safeParseColor(colorScheme.onBackgroundColorHex, 0xFFFFFFFF).copy(alpha = 0.1f)
        override val borderFocus: Color = safeParseColor(colorScheme.secondaryColorHex, 0xFFF58220)
        
        override val faceDetectionAligned: Color = safeParseColor(colorScheme.successColorHex, 0xFF4CAF50)
        override val faceDetectionMisaligned: Color = safeParseColor(colorScheme.errorColorHex, 0xFFD32F2F)
        override val faceSegmentComplete: Color = safeParseColor(colorScheme.successColorHex, 0xFF4CAF50)
        override val faceSegmentIncomplete: Color = safeParseColor(colorScheme.errorColorHex, 0xFFD32F2F)
        
        override val documentDetectionAligned: Color = safeParseColor(colorScheme.successColorHex, 0xFF4CAF50)
        override val documentDetectionMisaligned: Color = safeParseColor(colorScheme.errorColorHex, 0xFFD32F2F)
        
        override val gradientStart: Color = safeParseColor(colorScheme.backgroundColorHex, 0xFF22354D)
        override val gradientEnd: Color = safeParseColor(colorScheme.surfaceColorHex, 0xFF22354D)
    }
}

/**
 * Create the default artius.iD theme that matches the iOS standalone application
 */
private fun createDefaultArtiusIDTheme(): EnhancedSDKThemeConfiguration {
    return EnhancedSDKThemeConfiguration(
        brandName = "artius.iD",
        
        typography = SDKTypography(
            fontFamily = "default",
            headlineLarge = 32f,
            headlineMedium = 28f,
            titleLarge = 22f,
            bodyLarge = 16f,
            bodyMedium = 14f,
            headlineWeight = "bold",
            titleWeight = "medium",
            bodyWeight = "normal"
        ),
        
        colorScheme = SDKColorScheme(
            // CORRECTED FROM iOS SCREENSHOTS
            primaryColorHex = "#FFFFFF", // White - primary color should be white, not dark blue
            secondaryColorHex = "#F58220", // Orange from iOS screenshots - used for buttons and icons
            backgroundColorHex = "#22354D", // Dark blue background from iOS screenshots
            surfaceColorHex = "#22354D", // Dark blue surface matching background
            onPrimaryColorHex = "#22354D", // Dark blue text on white primary
            onSecondaryColorHex = "#FFFFFF", // White text on orange secondary
            onBackgroundColorHex = "#FFFFFF", // White text on dark blue background
            onSurfaceColorHex = "#FFFFFF", // White text on dark blue surface
            successColorHex = "#4CAF50",
            errorColorHex = "#D32F2F",
            warningColorHex = "#FF9800",
            primaryButtonColorHex = "#F58220", // Orange background - use secondary color for buttons
            primaryButtonTextColorHex = "#FFFFFF", // White text on orange buttons
            secondaryButtonColorHex = "#F58220", // Orange background - use secondary color for buttons
            secondaryButtonTextColorHex = "#FFFFFF" // White text on orange buttons
        ),
        
        iconTheme = SDKIconTheme(
            iconStyle = "default",
            mediumIconSize = 24f,
            primaryIconColorHex = "#F58220", // Orange - use secondary color for icons
            secondaryIconColorHex = "#F58220", // Orange - use secondary color for icons
            accentIconColorHex = "#F58220", // Orange - use secondary color for icons
            disabledIconColorHex = "#ADB5BD", // Light gray for disabled
            
            // Navigation & UI Icons - USE SECONDARY COLOR (ORANGE)
            navigationIconColorHex = "#F58220", // Orange - use secondary color for icons
            actionIconColorHex = "#F58220", // Orange - use secondary color for icons
            
            // Instruction & Guide Icons - USE SECONDARY COLOR (ORANGE)
            instructionIconColorHex = "#F58220", // Orange - use secondary color for icons
            warningIconColorHex = "#FF9800",
            errorIconColorHex = "#D32F2F",
            successIconColorHex = "#4CAF50",
            
            // Document & Verification Icons
            documentIconColorHex = "#F58220", // iOS Yellow900 - exact match
            cameraIconColorHex = "#FFFFFF", // iOS WhiteA700
            scanIconColorHex = "#F58220", // iOS Yellow900 - exact match
            
            // Biometric & Security Icons
            biometricIconColorHex = "#F58220", // iOS Yellow900 - exact match
            securityIconColorHex = "#4CAF50",
            nfcIconColorHex = "#F58220", // iOS Yellow900 - exact match
            
            // Status Icons
            statusActiveIconColorHex = "#4CAF50",
            statusInactiveIconColorHex = "#9E9E9E",
            statusProcessingIconColorHex = "#F58220", // iOS Yellow900 - exact match
            
            // Custom Icon Mappings for Authentication Screens
            customIcons = mapOf(
                "auth_success" to "approval", // Success screen image - high quality approval icon
                "auth_processing" to "img_processing" // Processing screen image (if needed)
            )
        ),
        
        textContent = SDKTextContent(
            welcomeTitle = "artius.iD Verification",
            welcomeSubtitle = "Secure identity verification powered by artius.iD",
            documentScanTitle = "Scan Your ID",
            passportScanTitle = "Scan Your Passport",
            faceScanTitle = "Face Verification",
            processingTitle = "Processing",
            verificationSuccessTitle = "Verification Complete"
        ),
        
        componentStyling = SDKComponentStyling(
            buttonCornerRadius = 8f,
            cardCornerRadius = 12f,
            buttonHeight = 48f
        ),
        
        layoutConfig = SDKLayoutConfig(
            screenPadding = 16f,
            componentSpacing = 16f
        )
    )
}
