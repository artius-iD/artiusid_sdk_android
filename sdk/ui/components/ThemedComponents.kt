/*
 * File: ThemedComponents.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.ui.theme.*

/**
 * Themed Button Component
 * Uses the enhanced theme configuration for styling
 */
@Composable
fun ThemedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    icon: String? = null
) {
    val themeConfig = LocalSDKTheme.current
    val componentStyling = LocalSDKComponentStyling.current
    val iconTheme = LocalSDKIconTheme.current
    val context = LocalContext.current
    
    val backgroundColor = when (style) {
        ButtonStyle.PRIMARY -> if (enabled) {
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.primaryButtonColorHex))
        } else {
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.disabledButtonColorHex))
        }
        ButtonStyle.SECONDARY -> if (enabled) {
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.secondaryButtonColorHex))
        } else {
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.disabledButtonColorHex))
        }
    }
    
    val textColor = when (style) {
        ButtonStyle.PRIMARY -> if (enabled) {
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.primaryButtonTextColorHex))
        } else {
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.disabledButtonTextColorHex))
        }
        ButtonStyle.SECONDARY -> if (enabled) {
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.secondaryButtonTextColorHex))
        } else {
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.disabledButtonTextColorHex))
        }
    }
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(componentStyling.buttonHeight.dp)
            .widthIn(min = componentStyling.buttonMinWidth.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor,
            disabledContentColor = textColor
        ),
        shape = RoundedCornerShape(componentStyling.buttonCornerRadius.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = componentStyling.buttonElevation.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(
                        id = EnhancedThemeManager.getIconResource(context, icon, iconTheme)
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(iconTheme.mediumIconSize.dp),
                    tint = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor
            )
        }
    }
}

/**
 * Themed Card Component
 */
@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val themeConfig = LocalSDKTheme.current
    val componentStyling = LocalSDKComponentStyling.current
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.surfaceColorHex))
        ),
        shape = RoundedCornerShape(componentStyling.cardCornerRadius.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = componentStyling.cardElevation.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Themed Text Components
 */
@Composable
fun ThemedHeadline(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val themeConfig = LocalSDKTheme.current
    
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        color = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onBackgroundColorHex)),
        textAlign = textAlign
    )
}

@Composable
fun ThemedTitle(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val themeConfig = LocalSDKTheme.current
    
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onBackgroundColorHex)),
        textAlign = textAlign
    )
}

@Composable
fun ThemedBody(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val themeConfig = LocalSDKTheme.current
    
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onBackgroundColorHex)),
        textAlign = textAlign
    )
}

@Composable
fun ThemedLabel(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val themeConfig = LocalSDKTheme.current
    
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onSurfaceVariantColorHex)),
        textAlign = textAlign
    )
}

/**
 * Themed Icon Component
 */
@Composable
fun ThemedIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    size: IconSize = IconSize.MEDIUM,
    tint: Color? = null
) {
    val themeConfig = LocalSDKTheme.current
    val iconTheme = LocalSDKIconTheme.current
    val context = LocalContext.current
    
    val iconSize = when (size) {
        IconSize.SMALL -> iconTheme.smallIconSize.dp
        IconSize.MEDIUM -> iconTheme.mediumIconSize.dp
        IconSize.LARGE -> iconTheme.largeIconSize.dp
        IconSize.EXTRA_LARGE -> iconTheme.extraLargeIconSize.dp
    }
    
    val iconTint = tint ?: Color(android.graphics.Color.parseColor(iconTheme.primaryIconColorHex))
    
    Icon(
        painter = painterResource(
            id = EnhancedThemeManager.getIconResource(context, iconName, iconTheme)
        ),
        contentDescription = null,
        modifier = modifier.size(iconSize),
        tint = iconTint
    )
}

/**
 * Themed Status Message Component
 */
@Composable
fun ThemedStatusMessage(
    message: String,
    status: MessageStatus,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    val themeConfig = LocalSDKTheme.current
    val componentStyling = LocalSDKComponentStyling.current
    
    val (backgroundColor, textColor, iconColor) = when (status) {
        MessageStatus.SUCCESS -> Triple(
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.successColorHex)),
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onSuccessColorHex)),
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onSuccessColorHex))
        )
        MessageStatus.ERROR -> Triple(
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.errorColorHex)),
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onErrorColorHex)),
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onErrorColorHex))
        )
        MessageStatus.WARNING -> Triple(
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.warningColorHex)),
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onWarningColorHex)),
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onWarningColorHex))
        )
        MessageStatus.INFO -> Triple(
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.infoColorHex)),
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onInfoColorHex)),
            Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onInfoColorHex))
        )
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(componentStyling.cardCornerRadius.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                ThemedIcon(
                    iconName = icon,
                    size = IconSize.MEDIUM,
                    tint = iconColor
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Themed Progress Indicator
 */
@Composable
fun ThemedProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true
) {
    val themeConfig = LocalSDKTheme.current
    val layoutConfig = LocalSDKLayoutConfig.current
    
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.primaryColorHex)),
            trackColor = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.surfaceVariantColorHex))
        )
        
        if (showPercentage) {
            Spacer(modifier = Modifier.height(layoutConfig.smallSpacing.dp))
            ThemedLabel(
                text = "${(progress * 100).toInt()}%",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Themed Top App Bar
 */
@Composable
fun ThemedTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val themeConfig = LocalSDKTheme.current
    val textContent = LocalSDKTextContent.current
    
    TopAppBar(
        title = {
            ThemedTitle(text = title)
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    ThemedIcon(
                        iconName = "back",
                        tint = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onSurfaceColorHex))
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.surfaceColorHex)),
            titleContentColor = Color(android.graphics.Color.parseColor(themeConfig.colorScheme.onSurfaceColorHex))
        )
    )
}

/**
 * Enums for component styling
 */
enum class ButtonStyle {
    PRIMARY,
    SECONDARY
}

enum class IconSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

enum class MessageStatus {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}
