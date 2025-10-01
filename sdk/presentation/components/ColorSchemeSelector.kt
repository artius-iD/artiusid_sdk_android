/*
 * File: ColorSchemeSelector.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ui.theme.*

/**
 * Color Scheme Selector Component
 * Allows users to switch between different color schemes
 */
@Composable
fun ColorSchemeSelector(
    modifier: Modifier = Modifier,
    onSchemeSelected: (ColorSchemeType) -> Unit = {}
) {
    var selectedScheme by remember { mutableStateOf(ColorManager.getCurrentSchemeType()) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = AppCardDefaults.cardColors(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Color Theme",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ColorManager.getAvailableSchemes()) { schemeType ->
                    ColorSchemePreview(
                        schemeType = schemeType,
                        isSelected = selectedScheme == schemeType,
                        onClick = {
                            selectedScheme = schemeType
                            ColorManager.setColorScheme(schemeType)
                            onSchemeSelected(schemeType)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSchemePreview(
    schemeType: ColorSchemeType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scheme = when (schemeType) {
        ColorSchemeType.DARK -> DarkColorScheme()
        ColorSchemeType.LIGHT -> LightColorScheme()
        ColorSchemeType.ALTERNATIVE -> AlternativeColorScheme()
    }
    
    val schemeName = when (schemeType) {
        ColorSchemeType.DARK -> "Dark"
        ColorSchemeType.LIGHT -> "Light"
        ColorSchemeType.ALTERNATIVE -> "Alternative"
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        // Color preview circles
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .background(
                    color = if (isSelected) AppColors.borderFocus else AppColors.border,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            // Primary color circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(scheme.primary)
            )
            
            // Secondary color circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(scheme.secondary)
            )
            
            // Background color circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(scheme.background)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = schemeName,
            fontSize = 12.sp,
            color = if (isSelected) AppColors.primary else AppColors.textSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Detailed Color Scheme Settings
 * Shows all color categories and their current values
 */
@Composable
fun ColorSchemeSettings(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = AppCardDefaults.cardColors(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Current Color Scheme",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Color categories
            ColorCategory("Primary Colors", listOf(
                "Primary" to AppColors.primary,
                "Secondary" to AppColors.secondary,
                "Background" to AppColors.background,
                "Surface" to AppColors.surface
            ))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ColorCategory("Text Colors", listOf(
                "Primary Text" to AppColors.textPrimary,
                "Secondary Text" to AppColors.textSecondary,
                "Disabled Text" to AppColors.textDisabled
            ))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ColorCategory("Status Colors", listOf(
                "Success" to AppColors.success,
                "Error" to AppColors.error,
                "Warning" to AppColors.warning,
                "Info" to AppColors.info
            ))
        }
    }
}

@Composable
private fun ColorCategory(
    title: String,
    colors: List<Pair<String, Color>>
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.textSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        colors.forEach { (name, color) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = name,
                    fontSize = 12.sp,
                    color = AppColors.textPrimary
                )
            }
        }
    }
}
