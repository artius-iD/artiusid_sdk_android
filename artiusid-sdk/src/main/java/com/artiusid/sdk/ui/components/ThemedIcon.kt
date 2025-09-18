/*
 * File: ThemedIcon.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.artiusid.sdk.ui.theme.ThemedIconColors

/**
 * General themed icon with customizable color and override support
 */
@Composable
fun ThemedIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    overrideKey: String? = null
) {
    val iconTint = tint ?: ThemedIconColors.getPrimaryIconColor()
    
    if (overrideKey != null) {
        // Use themed image with override support
        ThemedImage(
            defaultResourceId = iconRes,
            overrideKey = overrideKey,
            contentDescription = contentDescription ?: "",
            modifier = modifier,
            colorFilter = ColorFilter.tint(iconTint)
        )
    } else {
        // Fallback to standard image
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = modifier,
            colorFilter = ColorFilter.tint(iconTint)
        )
    }
}

/**
 * Navigation icons (back buttons, close buttons, etc.)
 */
@Composable
fun ThemedNavigationIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    overrideKey: String? = null
) {
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = ThemedIconColors.getNavigationIconColor(),
        overrideKey = overrideKey
    )
}

/**
 * Action icons (confirm, action buttons, etc.)
 */
@Composable
fun ThemedActionIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = ThemedIconColors.getActionIconColor()
    )
}

/**
 * Instruction icons (tips, guides, info icons)
 */
@Composable
fun ThemedInstructionIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = ThemedIconColors.getInstructionIconColor()
    )
}

/**
 * Document-related icons
 */
@Composable
fun ThemedDocumentIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    overrideKey: String? = null
) {
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = ThemedIconColors.getDocumentIconColor(),
        overrideKey = overrideKey
    )
}

/**
 * Camera-related icons
 */
@Composable
fun ThemedCameraIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = ThemedIconColors.getCameraIconColor()
    )
}

/**
 * Scanning overlay icons
 */
@Composable
fun ThemedScanIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = ThemedIconColors.getScanIconColor()
    )
}

/**
 * Biometric icons (face scan, fingerprint, etc.)
 */
@Composable
fun ThemedBiometricIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = ThemedIconColors.getBiometricIconColor()
    )
}

/**
 * Security icons (lock, security, etc.)
 */
@Composable
fun ThemedSecurityIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = ThemedIconColors.getSecurityIconColor()
    )
}

/**
 * NFC-related icons
 */
@Composable
fun ThemedNfcIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = ThemedIconColors.getNfcIconColor()
    )
}

/**
 * Status icons with specific states
 */
@Composable
fun ThemedStatusIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    status: IconStatus = IconStatus.ACTIVE
) {
    val iconColor = when (status) {
        IconStatus.ACTIVE -> ThemedIconColors.getStatusActiveIconColor()
        IconStatus.INACTIVE -> ThemedIconColors.getStatusInactiveIconColor()
        IconStatus.PROCESSING -> ThemedIconColors.getStatusProcessingIconColor()
        IconStatus.SUCCESS -> ThemedIconColors.getSuccessIconColor()
        IconStatus.ERROR -> ThemedIconColors.getErrorIconColor()
        IconStatus.WARNING -> ThemedIconColors.getWarningIconColor()
    }
    
    ThemedIcon(
        iconRes = iconRes,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = iconColor
    )
}

/**
 * Icon status enumeration
 */
enum class IconStatus {
    ACTIVE,
    INACTIVE,
    PROCESSING,
    SUCCESS,
    ERROR,
    WARNING
}