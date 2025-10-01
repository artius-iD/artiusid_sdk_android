/*
 * File: SelectDocumentTypeScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.presentation.components.AppTopBar
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.ui.components.ThemedDocumentIcon
import com.artiusid.sdk.ui.components.ThemedActionIcon
import com.artiusid.sdk.ui.components.ThemedSecurityIcon

@Composable
fun SelectDocumentTypeScreen(
    onNavigateToDocumentScan: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom back button
            CustomBackButton(
                onBackClick = onNavigateBack,
                navTitle = "Select Document"
            )
            
            // Title matching iOS exact styling
            Text(
                text = "Please select the type of document you want to scan",
                color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 80.dp,
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp
                    )
            )
            
            // State ID Option - matching iOS Grid layout exactly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(
                        width = 0.4.dp,
                        color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable { onNavigateToDocumentScan("id") }
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemedDocumentIcon(
                        iconRes = R.drawable.stateid_icon,
                        contentDescription = "State ID",
                        overrideKey = "state_id_icon",
                        modifier = Modifier
                            .size(width = 50.dp, height = 35.dp)
                            .padding(end = 16.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "State ID",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        Text(
                            text = "Need to scan the front and back of your ID",
                            fontSize = 16.sp,
                            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    ThemedActionIcon(
                        iconRes = R.drawable.document_right_arrow,
                        contentDescription = "Arrow",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 8.dp)
                    )
                }
            }
            
            // Passport Option - matching iOS Grid layout exactly
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(
                        width = 0.4.dp,
                        color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable { onNavigateToDocumentScan("passport") }
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemedDocumentIcon(
                        iconRes = R.drawable.passport_icon,
                        contentDescription = "Passport",
                        overrideKey = "passport_icon",
                        modifier = Modifier
                            .size(width = 35.dp, height = 50.dp)
                            .padding(end = 16.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Passport",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        Text(
                            text = "Need to scan your passport document",
                            fontSize = 16.sp,
                            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    ThemedActionIcon(
                        iconRes = R.drawable.document_right_arrow,
                        contentDescription = "Arrow",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Privacy notice - matching iOS Label exactly
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                ThemedSecurityIcon(
                    iconRes = R.drawable.lock_icon,
                    contentDescription = "Lock",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "This is just to validate your identity in the verification process, your information will not be saved anywhere.",
                    color = com.artiusid.sdk.ui.theme.ThemedTextColors.getSecondaryTextColor(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CustomBackButton(
    onBackClick: () -> Unit,
    navTitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor()
            )
        }
        
        Text(
            text = navTitle,
            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
} 