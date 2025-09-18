/*
 * File: Theme.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Main theme composable that integrates with the global color scheme system
 */
@Composable
fun ArtiusIDTheme(
    colorSchemeType: ColorSchemeType? = null, // Allow override of color scheme
    darkTheme: Boolean = true, // Default to dark theme like iOS
    dynamicColor: Boolean = false, // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
    // Set the color scheme based on parameters
    val appColorScheme = when {
        colorSchemeType != null -> {
            ColorManager.setColorScheme(colorSchemeType)
            ColorManager.getCurrentScheme()
        }
        else -> {
            // Use current scheme or set default based on darkTheme
            val defaultScheme = if (darkTheme) ColorSchemeType.DARK else ColorSchemeType.LIGHT
            ColorManager.setColorScheme(defaultScheme)
            ColorManager.getCurrentScheme()
        }
    }
    
    // Convert app color scheme to Material3 color scheme
    val materialColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = appColorScheme.primary,
            secondary = appColorScheme.secondary,
            tertiary = appColorScheme.info,
            background = appColorScheme.background,
            surface = appColorScheme.surface,
            onPrimary = appColorScheme.onPrimary,
            onSecondary = appColorScheme.onSecondary,
            onBackground = appColorScheme.onBackground,
            onSurface = appColorScheme.onSurface,
            error = appColorScheme.error,
            onError = appColorScheme.onError
        )
        else -> lightColorScheme(
            primary = appColorScheme.primary,
            secondary = appColorScheme.secondary,
            tertiary = appColorScheme.info,
            background = appColorScheme.background,
            surface = appColorScheme.surface,
            onPrimary = appColorScheme.onPrimary,
            onSecondary = appColorScheme.onSecondary,
            onBackground = appColorScheme.onBackground,
            onSurface = appColorScheme.onSurface,
            error = appColorScheme.error,
            onError = appColorScheme.onError
        )
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = materialColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Provide both Material3 theme and app color scheme
    ProvideAppColorScheme(colorScheme = appColorScheme) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
} 