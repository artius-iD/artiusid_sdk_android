/*
 * File: VerificationStepsScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.verification

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.R
import com.artiusid.sdk.presentation.components.AppTopBar
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.utils.LocalizationManager
import androidx.compose.ui.platform.LocalContext
import com.artiusid.sdk.ui.components.ThemedIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationStepsScreen(
    onNavigateToFaceScan: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    GradientBackground {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = LocalizationManager.getString(context, "verification_steps_title", "Verification Steps"),
                    onBackClick = onNavigateBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = LocalizationManager.getString(context, "verification_steps_subtitle", "Follow these steps to verify your identity"),
                    fontSize = getRelativeFontSize(18f).sp,
                    fontWeight = FontWeight.Bold,
                    color = com.artiusid.sdk.ui.theme.ThemedTextColors.getSecondaryTextColor(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                // Step 1: Face Scan - responsive layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemedIcon(
                        iconRes = R.drawable.scan_face_icon,
                        contentDescription = "Face Scan",
                        overrideKey = "scan_face_icon",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = LocalizationManager.getString(context, "step_face_scan", "Face Scan"),
                            fontSize = getRelativeFontSize(18f).sp,
                            fontWeight = FontWeight.Bold,
                            color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                        )
                        Text(
                            text = LocalizationManager.getString(context, "step_face_scan_description", "Scan your face for verification"),
                            fontSize = getRelativeFontSize(14f).sp,
                            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor()
                        )
                    }
                }

                // Step 2: Document Scan - responsive layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemedIcon(
                        iconRes = R.drawable.doc_scan_icon,
                        contentDescription = "Document Scan",
                        overrideKey = "doc_scan_icon",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = LocalizationManager.getString(context, "step_document_scan", "Document Scan"),
                            fontSize = getRelativeFontSize(18f).sp,
                            fontWeight = FontWeight.Bold,
                            color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                        )
                        Text(
                            text = LocalizationManager.getString(context, "step_document_scan_description", "Scan your ID document"),
                            fontSize = getRelativeFontSize(14f).sp,
                            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor()
                        )
                    }
                }

                // Step 3: Completion - responsive layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemedIcon(
                        iconRes = R.drawable.done_icon,
                        contentDescription = "Completion",
                        overrideKey = "done_icon",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = LocalizationManager.getString(context, "step_processing", "Completion"),
                            fontSize = getRelativeFontSize(18f).sp,
                            fontWeight = FontWeight.Bold,
                            color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                        )
                        Text(
                            text = LocalizationManager.getString(context, "step_processing_description", "Complete the verification process"),
                            fontSize = getRelativeFontSize(14f).sp,
                            color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor()
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Start Now Button - responsive dimensions
                Button(
                    onClick = onNavigateToFaceScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = LocalizationManager.getString(context, "button_start_now", "Start Now"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
} 