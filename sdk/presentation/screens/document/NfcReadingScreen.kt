/*
 * File: NfcReadingScreen.kt
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
fun NfcReadingScreen(
    onNavigateToFaceVerification: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isReading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Simulate NFC reading process
        kotlinx.coroutines.delay(2000)
        isReading = false
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "NFC Reading",
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
            if (isReading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Reading NFC chip...\nPlease hold your document steady",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "NFC reading successful!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNavigateToFaceVerification,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue to Face Verification")
                }
            }
        }
    }
} 