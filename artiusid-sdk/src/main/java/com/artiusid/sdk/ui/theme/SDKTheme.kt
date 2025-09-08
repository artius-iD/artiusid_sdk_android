package com.artiusid.sdk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * SDK Theme data class
 */
data class SDKTheme(
    val primaryColor: Color = Color(0xFF2D3748),
    val secondaryColor: Color = Color(0xFFFF6B35),
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val textSecondaryColor: Color = Color(0xFF6B7280),
    val buttonStyle: String = "FILLED",
    val progressStyle: String = "CIRCULAR",
    val darkModeSupport: Boolean = true,
    val errorColor: Color = Color(0xFFD32F2F),
    val surfaceColor: Color = Color(0xFFF5F5F5),
    val fontFamily: Int? = null, // Resource ID for font
    val cornerRadius: Float = 8.0f
)

@Composable
fun ArtiusIDSDKTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}
