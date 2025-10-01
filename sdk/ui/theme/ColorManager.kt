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
import com.artiusid.sdk.models.SDKTypography
import com.artiusid.sdk.models.SDKIconTheme
import com.artiusid.sdk.models.SDKTextContent
import com.artiusid.sdk.models.SDKComponentStyling
import com.artiusid.sdk.models.SDKLayoutConfig

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
    
    // Default artius.iD theme that matches iOS standalone app
    private val defaultArtiusIDTheme = createDefaultArtiusIDTheme()
    
    init {
        // Apply default artius.iD theme on initialization
        setEnhancedTheme(defaultArtiusIDTheme)
    }
    
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
