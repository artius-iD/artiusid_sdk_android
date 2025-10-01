/*
 * File: SettingsScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.data.repository.LogManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToApprovalRequest: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLogDialog by remember { mutableStateOf(false) }
    var showLogShareDialog by remember { mutableStateOf(false) }
    var showApprovalResult by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                com.artiusid.sdk.ui.theme.ColorManager.getGradientBrush()
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(40.dp)) // For symmetry
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Environment Settings
        CardSection(title = "Environment Settings") {
            EnvironmentDropdown(
                selected = uiState.environment,
                onSelected = { viewModel.setEnvironment(it) },
                environments = uiState.environmentOptions
            )
            if (uiState.isCertCleared) {
                Text("Certificate Cleared", color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(), fontSize = MaterialTheme.typography.bodySmall.fontSize)
            } else if (uiState.isCertClearing) {
                CircularProgressIndicator(color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(), modifier = Modifier.size(20.dp))
            }
        }

        // 2. Application Modes
        CardSection(title = "Application Modes") {
            SettingSwitch(
                label = "Run in Developer Mode",
                checked = uiState.isDeveloperMode,
                onCheckedChange = { viewModel.setDeveloperMode(it) }
            )
            SettingSwitch(
                label = "Run in Demo Mode",
                checked = uiState.isDemoMode,
                onCheckedChange = { viewModel.setDemoMode(it) }
            )
            SettingSwitch(
                label = "Enable S3",
                checked = uiState.enableS3,
                onCheckedChange = { viewModel.setEnableS3(it) }
            )
        }

        // 3. Image Capture
        CardSection(title = "Image Capture") {
            SettingSwitch(
                label = "Display Overlays",
                checked = uiState.displayImageOverlays,
                onCheckedChange = { viewModel.setDisplayImageOverlays(it) }
            )
            SettingSwitch(
                label = "Outline Documents",
                checked = uiState.displayDocumentOutline,
                onCheckedChange = { viewModel.setDisplayDocumentOutline(it) }
            )
            SettingSwitch(
                label = "Face and Barcode Overlay",
                checked = uiState.displayTargetObjectOutline,
                onCheckedChange = { viewModel.setDisplayTargetObjectOutline(it) }
            )
        }

        // 4. Approval Request
        CardSection(title = "Approval Request") {
            
            // Firebase Status (for debugging real server integration)
            var firebaseStatus by remember { mutableStateOf("Checking Firebase...") }
            
            LaunchedEffect(Unit) {
                firebaseStatus = com.artiusid.sdk.utils.FirebaseTestManager.checkFirebaseStatus(context)
            }
            
            Text(
                text = firebaseStatus,
                color = Color.White,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        // Real server integration: Don't navigate here
                        // Navigation will happen automatically when Firebase notification is received
                        // Approval request functionality moved to SDK public method
                        // viewModel.sendApprovalRequest { ... }
                        showApprovalResult = true
                    }
                },
                enabled = !uiState.isApprovalLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                    containerColor = Color.Transparent
                )
            ) {
                if (uiState.isApprovalLoading) {
                    CircularProgressIndicator(color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sending...", color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor())
                } else {
                    Text("Send Approval Request", color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor())
                }
            }
            // Show result message automatically when available (like iOS)
            if (uiState.approvalResultMessage.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = uiState.approvalResultMessage,
                        color = if (uiState.isApprovalSuccess) Color.Green else Color.Red,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                    if (uiState.isApprovalSuccess) {
                        Text(
                            text = "⏳ Waiting for Firebase notification...",
                            color = Color.Yellow,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // 5. Developer Mode View
        if (uiState.isDeveloperMode) {
            CardSection(title = "Developer Info") {
                Text("Device ID: ${uiState.deviceId}", color = Color.White)
                Text("Environment: ${uiState.environment}", color = Color.White)
                Text("ClientId: ${uiState.clientId} | ClientGroupId: ${uiState.clientGroupId}", color = Color.White)
                if (uiState.accountNumber.isNotEmpty()) {
                    Text("Account Number: ${uiState.accountNumber}", color = Color.White)
                }
            }
        }

        // 6. Log Settings
        CardSection(title = "Log Settings") {
            LogLevelDropdown(
                selected = uiState.logLevel,
                onSelected = { viewModel.setLogLevel(it) },
                logLevels = uiState.logLevelOptions
            )
            Text(uiState.logLevelDescription, color = Color.Gray, fontSize = MaterialTheme.typography.bodySmall.fontSize)
        }

        // 7. Log Viewer
        CardSection(title = "Debug Logs") {
            Column(modifier = Modifier.fillMaxWidth()) {
                // View Logs Button
                OutlinedButton(
                    onClick = {
                        viewModel.refreshLogs()
                        showLogDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                        containerColor = Color.Transparent
                    )
                ) {
                    Text("View Logs", color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor())
                }
                
                // Share Logs Button
                OutlinedButton(
                    onClick = { showLogShareDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                        containerColor = Color.Transparent
                    )
                ) {
                    Text("Share Logs", color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor())
                }
                
                // Clear Logs Button
                OutlinedButton(
                    onClick = { viewModel.clearLogs() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                        containerColor = Color.Transparent
                    )
                ) {
                    Text("Clear Logs", color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor())
                }
            }
            Text("Log count: ${uiState.logCount}", color = Color.White, fontSize = MaterialTheme.typography.bodySmall.fontSize)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("END OF SCREEN", color = Color.Transparent, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
    }

    // Log Viewer Dialog
    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("Debug Logs") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(uiState.logs) { log ->
                        Text(
                            text = log,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLogDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showLogShareDialog) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            try {
                // Create a temporary file with logs using enhanced export functionality
                val logFile = File(context.cacheDir, "artius.iD_Debug_Logs.txt")
                FileWriter(logFile).use { writer ->
                    // Use LogManager's export functionality for better formatting
                    val exportedLogs = LogManager.exportLogs()
                    writer.write(exportedLogs)
                }

                // Create share intent
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    logFile
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "artius.iD Debug Logs")
                    putExtra(Intent.EXTRA_TEXT, "Debug logs from artius.iD app")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share Logs"))
                showLogShareDialog = false
            } catch (e: Exception) {
                Log.e("SettingsScreen", "Error sharing logs", e)
                showLogShareDialog = false
            }
        }
    }
}

@Composable
fun CardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                checkedTrackColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor().copy(alpha = 0.5f),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun EnvironmentDropdown(selected: String, onSelected: (String) -> Unit, environments: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected, color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor())
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor())
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            environments.forEach { env ->
                DropdownMenuItem(
                    text = { Text(env) },
                    onClick = {
                        onSelected(env)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LogLevelDropdown(selected: String, onSelected: (String) -> Unit, logLevels: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected, color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor())
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor())
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            logLevels.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level) },
                    onClick = {
                        onSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
} 