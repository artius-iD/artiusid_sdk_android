/*
 * File: BrandNameDisplay.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.artiusid.sdk.ui.theme.LocalSDKTheme

/**
 * Composable that displays the brand name from theme configuration
 * Automatically splits brand names with dots or spaces to highlight the second part
 * 
 * @param modifier Modifier for the container
 * @param style Text style to apply
 * @param primaryColor Color for the first part of the brand name
 * @param accentColor Color for the second part of the brand name (if split)
 * @param fontWeight Font weight to apply
 * @param textAlign Text alignment
 */
@Composable
fun BrandNameDisplay(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    primaryColor: Color = MaterialTheme.colorScheme.onBackground,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Center
) {
    val themeConfig = LocalSDKTheme.current
    val brandName = themeConfig.brandName
    
    // Split brand name to highlight the last part (e.g., "artius" and "iD")
    val parts = brandName.split(".", " ", limit = 2)
    
    if (parts.size >= 2) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = parts[0],
                style = style,
                fontWeight = fontWeight,
                color = primaryColor,
                textAlign = textAlign
            )
            Text(
                text = parts[1],
                style = style,
                fontWeight = fontWeight,
                color = accentColor,
                textAlign = textAlign
            )
        }
    } else {
        // Fallback for single-word brand names
        Text(
            text = brandName,
            modifier = modifier,
            style = style,
            fontWeight = fontWeight,
            color = accentColor,
            textAlign = textAlign
        )
    }
}

/**
 * Simple brand name text without splitting
 */
@Composable
fun SimpleBrandNameText(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = MaterialTheme.colorScheme.primary,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Center
) {
    val themeConfig = LocalSDKTheme.current
    
    Text(
        text = themeConfig.brandName,
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign
    )
}
