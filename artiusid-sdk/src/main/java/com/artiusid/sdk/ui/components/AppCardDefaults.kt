package com.artiusid.sdk.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ui.theme.SDKColors

/**
 * Default card styling for the SDK
 */
object AppCardDefaults {
    
    /**
     * Default card colors
     */
    @Composable
    fun cardColors() = CardDefaults.cardColors(
        containerColor = SDKColors.WhiteA700,
        contentColor = SDKColors.TextPrimary
    )
    
    /**
     * Default card elevation
     */
    @Composable
    fun cardElevation(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = 4.dp,
        pressedElevation = 8.dp,
        focusedElevation = 6.dp,
        hoveredElevation = 6.dp,
        draggedElevation = 8.dp,
        disabledElevation = 0.dp
    )
    
    /**
     * Default card shape
     */
    val cardShape: Shape = RoundedCornerShape(12.dp)
    
    /**
     * Rounded card shape
     */
    val roundedCardShape: Shape = RoundedCornerShape(16.dp)
    
    /**
     * Small card shape
     */
    val smallCardShape: Shape = RoundedCornerShape(8.dp)
}
