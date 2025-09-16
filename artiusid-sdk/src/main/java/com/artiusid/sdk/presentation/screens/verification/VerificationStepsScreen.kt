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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationStepsScreen(
    onNavigateToFaceScan: () -> Unit,
    onNavigateBack: () -> Unit
) {
    GradientBackground {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Verification Steps",
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
                    text = "Follow these steps to verify your identity",
                    fontSize = getRelativeFontSize(18f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray500,
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
                    Image(
                        painter = painterResource(id = R.drawable.scan_face_icon),
                        contentDescription = "Face Scan",
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
                            text = "Face Scan",
                            fontSize = getRelativeFontSize(18f).sp,
                            fontWeight = FontWeight.Bold,
                            color = Yellow900
                        )
                        Text(
                            text = "Scan your face for verification",
                            fontSize = getRelativeFontSize(14f).sp,
                            color = WhiteA700
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
                    Image(
                        painter = painterResource(id = R.drawable.doc_scan_icon),
                        contentDescription = "Document Scan",
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
                            text = "Document Scan",
                            fontSize = getRelativeFontSize(18f).sp,
                            fontWeight = FontWeight.Bold,
                            color = Yellow900
                        )
                        Text(
                            text = "Scan your ID document",
                            fontSize = getRelativeFontSize(14f).sp,
                            color = WhiteA700
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
                    Image(
                        painter = painterResource(id = R.drawable.done_icon),
                        contentDescription = "Completion",
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
                            text = "Completion",
                            fontSize = getRelativeFontSize(18f).sp,
                            fontWeight = FontWeight.Bold,
                            color = Yellow900
                        )
                        Text(
                            text = "Complete the verification process",
                            fontSize = getRelativeFontSize(14f).sp,
                            color = WhiteA700
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
                        containerColor = Yellow900
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Start Now",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
} 