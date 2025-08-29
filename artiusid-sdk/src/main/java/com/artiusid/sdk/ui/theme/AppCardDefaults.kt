package com.artiusid.sdk.ui.theme

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Default styling for SDK cards
 */
object AppCardDefaults {
    
    @Composable
    fun cardColors(
        containerColor: Color = Color.White,
        contentColor: Color = Color.Black
    ): CardColors {
        return CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    }
}
