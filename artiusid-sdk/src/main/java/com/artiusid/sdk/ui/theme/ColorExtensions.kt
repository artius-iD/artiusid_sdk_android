/*
 * File: ColorExtensions.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Extension functions and utilities for easy color access in Material3 components
 */

/**
 * Button color defaults using app color scheme
 */
object AppButtonDefaults {
    @Composable
    fun primaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = AppColors.buttonPrimary,
        contentColor = AppColors.buttonTextPrimary,
        disabledContainerColor = AppColors.buttonDisabled,
        disabledContentColor = AppColors.buttonTextDisabled
    )
    
    @Composable
    fun secondaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = AppColors.buttonSecondary,
        contentColor = AppColors.buttonTextSecondary,
        disabledContainerColor = AppColors.buttonDisabled,
        disabledContentColor = AppColors.buttonTextDisabled
    )
    
    @Composable
    fun outlinedButtonColors() = ButtonDefaults.outlinedButtonColors(
        contentColor = AppColors.buttonTextSecondary,
        disabledContentColor = AppColors.buttonTextDisabled
    )
}

/**
 * Card color defaults using app color scheme
 */
object AppCardDefaults {
    @Composable
    fun cardColors() = CardDefaults.cardColors(
        containerColor = AppColors.surface,
        contentColor = AppColors.onSurface
    )
    
    @Composable
    fun elevatedCardColors() = CardDefaults.elevatedCardColors(
        containerColor = AppColors.surface,
        contentColor = AppColors.onSurface
    )
}

/**
 * Text field color defaults using app color scheme
 */
object AppTextFieldDefaults {
    @Composable
    fun textFieldColors() = TextFieldDefaults.colors(
        focusedTextColor = AppColors.textPrimary,
        unfocusedTextColor = AppColors.textPrimary,
        disabledTextColor = AppColors.textDisabled,
        focusedContainerColor = AppColors.surface,
        unfocusedContainerColor = AppColors.surface,
        disabledContainerColor = AppColors.surface,
        focusedIndicatorColor = AppColors.borderFocus,
        unfocusedIndicatorColor = AppColors.border,
        disabledIndicatorColor = AppColors.borderLight
    )
    
    @Composable
    fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppColors.textPrimary,
        unfocusedTextColor = AppColors.textPrimary,
        disabledTextColor = AppColors.textDisabled,
        focusedBorderColor = AppColors.borderFocus,
        unfocusedBorderColor = AppColors.border,
        disabledBorderColor = AppColors.borderLight
    )
}

/**
 * Status color utilities
 */
object AppStatusColors {
    val success: Color @Composable get() = AppColors.success
    val error: Color @Composable get() = AppColors.error
    val warning: Color @Composable get() = AppColors.warning
    val info: Color @Composable get() = AppColors.info
    
    val onSuccess: Color @Composable get() = AppColors.onSuccess
    val onError: Color @Composable get() = AppColors.onError
    val onWarning: Color @Composable get() = AppColors.onWarning
    val onInfo: Color @Composable get() = AppColors.onInfo
}

/**
 * Face detection color utilities
 */
object AppFaceColors {
    val aligned: Color @Composable get() = AppColors.faceDetectionAligned
    val misaligned: Color @Composable get() = AppColors.faceDetectionMisaligned
    val segmentComplete: Color @Composable get() = AppColors.faceSegmentComplete
    val segmentIncomplete: Color @Composable get() = AppColors.faceSegmentIncomplete
}

/**
 * Document detection color utilities
 */
object AppDocumentColors {
    val aligned: Color @Composable get() = AppColors.documentDetectionAligned
    val misaligned: Color @Composable get() = AppColors.documentDetectionMisaligned
}

/**
 * Overlay color utilities
 */
object AppOverlayColors {
    val overlay: Color @Composable get() = AppColors.overlay
    val overlayLight: Color @Composable get() = AppColors.overlayLight
    val scrim: Color @Composable get() = AppColors.scrim
}
