/*
 * File: SDKThemeProvider.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import com.artiusid.sdk.ui.theme.SDKTheme
import com.artiusid.sdk.managers.SDKConfigManager

/**
 * Themeable UI system for the SDK
 */
object SDKThemeProvider {
    
    @Composable
    fun ArtiusSDKTheme(
        content: @Composable () -> Unit
    ) {
        val sdkTheme = SDKConfigManager.getTheme()
        val context = LocalContext.current
        
        // Create Material 3 color scheme from SDK theme
        val colorScheme = createColorScheme(sdkTheme, isSystemInDarkTheme())
        
        // Create typography from SDK theme
        val typography = createTypography(sdkTheme, context)
        
        // Create shapes from SDK theme
        val shapes = createShapes(sdkTheme)
        
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
    
    @Composable
    private fun createColorScheme(sdkTheme: SDKTheme, isDarkTheme: Boolean): ColorScheme {
        return if (isDarkTheme && sdkTheme.darkModeSupport) {
            darkColorScheme(
                primary = sdkTheme.primaryColor,
                secondary = sdkTheme.secondaryColor,
                background = Color.Black,
                surface = Color(0xFF121212),
                error = sdkTheme.errorColor,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color.White,
                onSurface = Color.White,
                onError = Color.White
            )
        } else {
            lightColorScheme(
                primary = sdkTheme.primaryColor,
                secondary = sdkTheme.secondaryColor,
                background = sdkTheme.backgroundColor,
                surface = sdkTheme.surfaceColor,
                error = sdkTheme.errorColor,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = sdkTheme.textColor,
                onSurface = sdkTheme.textColor,
                onError = Color.White
            )
        }
    }
    
    @Composable
    private fun createTypography(sdkTheme: SDKTheme, context: android.content.Context): Typography {
        val fontFamily = sdkTheme.fontFamily?.let { fontRes ->
            try {
                FontFamily(Font(fontRes))
            } catch (e: Exception) {
                FontFamily.Default
            }
        } ?: FontFamily.Default
        
        return Typography(
            displayLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = sdkTheme.textColor
            ),
            displayMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = sdkTheme.textColor
            ),
            displaySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = sdkTheme.textColor
            ),
            headlineLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = sdkTheme.textColor
            ),
            headlineMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = sdkTheme.textColor
            ),
            headlineSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = sdkTheme.textColor
            ),
            titleLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = sdkTheme.textColor
            ),
            titleMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = sdkTheme.textColor
            ),
            titleSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = sdkTheme.textColor
            ),
            bodyLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = sdkTheme.textColor
            ),
            bodyMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = sdkTheme.textColor
            ),
            bodySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = sdkTheme.textSecondaryColor
            ),
            labelLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = sdkTheme.textColor
            ),
            labelMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = sdkTheme.textColor
            ),
            labelSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = sdkTheme.textSecondaryColor
            )
        )
    }
    
    @Composable
    private fun createShapes(sdkTheme: SDKTheme): Shapes {
        return Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(sdkTheme.cornerRadius * 0.5f),
            small = androidx.compose.foundation.shape.RoundedCornerShape(sdkTheme.cornerRadius),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(sdkTheme.cornerRadius * 1.5f),
            large = androidx.compose.foundation.shape.RoundedCornerShape(sdkTheme.cornerRadius * 2f),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(sdkTheme.cornerRadius * 3f)
        )
    }
}

/**
 * Themeable SDK components
 */
@Composable
fun SDKButton(
    onClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val sdkTheme = SDKConfigManager.getTheme()
    
    when (sdkTheme.buttonStyle) {
        "FILLED" -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = sdkTheme.primaryColor,
                    contentColor = Color.White
                ),
                content = { content() }
            )
        }
        "OUTLINED" -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = sdkTheme.primaryColor
                ),
                content = { content() }
            )
        }
        "TEXT" -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = sdkTheme.primaryColor
                ),
                content = { content() }
            )
        }
        "ROUNDED" -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = sdkTheme.primaryColor,
                    contentColor = Color.White
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(sdkTheme.cornerRadius * 2f),
                content = { content() }
            )
        }
    }
}

@Composable
fun SDKProgressIndicator(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    progress: Float? = null
) {
    val sdkTheme = SDKConfigManager.getTheme()
    
    when (sdkTheme.progressIndicatorStyle) {
        "CIRCULAR" -> {
            if (progress != null) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = modifier,
                    color = sdkTheme.primaryColor
                )
            } else {
                CircularProgressIndicator(
                    modifier = modifier,
                    color = sdkTheme.primaryColor
                )
            }
        }
        "LINEAR" -> {
            if (progress != null) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = modifier,
                    color = sdkTheme.primaryColor
                )
            } else {
                LinearProgressIndicator(
                    modifier = modifier,
                    color = sdkTheme.primaryColor
                )
            }
        }
        "CUSTOM" -> {
            // Custom progress indicator implementation
            if (progress != null) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = modifier,
                    color = sdkTheme.primaryColor
                )
            } else {
                CircularProgressIndicator(
                    modifier = modifier,
                    color = sdkTheme.primaryColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SDKCard(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val sdkTheme = SDKConfigManager.getTheme()
    
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        colors = CardDefaults.cardColors(
            containerColor = sdkTheme.surfaceColor
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(sdkTheme.cornerRadius),
        content = { content() }
    )
}

@Composable
fun SDKLogo(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val sdkTheme = SDKConfigManager.getTheme()
    val context = LocalContext.current
    
    sdkTheme.logo?.let { logoRes ->
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(logoRes),
            contentDescription = "Company Logo",
            modifier = modifier
        )
    } ?: run {
        // Default text logo
        Text(
            text = "ArtiusID",
            style = MaterialTheme.typography.headlineMedium,
            color = sdkTheme.primaryColor,
            modifier = modifier
        )
    }
}

@Composable
fun SDKStatusIndicator(
    status: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val sdkTheme = SDKConfigManager.getTheme()
    
    val (color, icon) = when (status.uppercase()) {
        "SUCCESS", "COMPLETED", "VERIFIED" -> Pair(sdkTheme.successColor, "✓")
        "ERROR", "FAILED", "REJECTED" -> Pair(sdkTheme.errorColor, "✗")
        "WARNING", "PENDING", "PROCESSING" -> Pair(sdkTheme.warningColor, "⚠")
        else -> Pair(sdkTheme.textSecondaryColor, "○")
    }
    
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            color = color,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
        Text(
            text = status,
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
