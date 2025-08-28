package com.artiusid.sdk.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ui.theme.SDKColors

/**
 * Top bar component used throughout the standalone app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = "",
    onBackClick: (() -> Unit)? = null,
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = SDKColors.WhiteA700,
    showBackButton: Boolean = true
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = contentColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            if (showBackButton && onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = contentColor
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor
        )
    )
}

/**
 * Simple top bar without Material3 styling
 */
@Composable
fun SimpleTopBar(
    title: String = "",
    onBackClick: (() -> Unit)? = null,
    contentColor: Color = SDKColors.WhiteA700
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = contentColor
                )
            }
        }
        
        Text(
            text = title,
            color = contentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        
        // Spacer to balance the back button
        if (onBackClick != null) {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}