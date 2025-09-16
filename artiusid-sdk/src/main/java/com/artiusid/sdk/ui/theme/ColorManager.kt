package com.artiusid.sdk.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush

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
    
    /**
     * Get the current color scheme
     */
    fun getCurrentScheme(): AppColorScheme = currentScheme
    
    /**
     * Get the current scheme type
     */
    fun getCurrentSchemeType(): ColorSchemeType = currentSchemeType
    
    /**
     * Switch to a different color scheme
     */
    fun setColorScheme(schemeType: ColorSchemeType) {
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
        return Brush.verticalGradient(
            colors = listOf(
                currentScheme.gradientStart,
                currentScheme.gradientEnd
            )
        )
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
