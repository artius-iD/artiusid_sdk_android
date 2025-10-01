/*
 * File: LocalizedText.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.artiusid.sdk.localization.getLocalizedString

/**
 * Composable for displaying localized text that can be overridden by the host application
 */
@Composable
fun LocalizedText(
    @StringRes stringRes: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    style: TextStyle = androidx.compose.material3.LocalTextStyle.current,
    vararg formatArgs: Any
) {
    val context = LocalContext.current
    val text = if (formatArgs.isNotEmpty()) {
        context.getLocalizedString(stringRes, *formatArgs)
    } else {
        context.getLocalizedString(stringRes)
    }
    
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        style = style
    )
}

/**
 * Helper function to get localized string from Composable context
 */
@Composable
fun localizedString(@StringRes stringRes: Int, vararg formatArgs: Any): String {
    val context = LocalContext.current
    return if (formatArgs.isNotEmpty()) {
        context.getLocalizedString(stringRes, *formatArgs)
    } else {
        context.getLocalizedString(stringRes)
    }
}
