/*
 * File: DocumentVerificationScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.presentation.components.AppTopBar

@Composable
fun DocumentVerificationScreen(
    onNavigateToNfcReading: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isVerifying by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Simulate verification process
        kotlinx.coroutines.delay(2000)
        isVerifying = false
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Document Verification",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isVerifying) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Verifying your document...",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Document verified successfully!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNavigateToNfcReading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue to NFC Reading")
                }
            }
        }
    }
} 