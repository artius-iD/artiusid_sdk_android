/*
 * File: NotificationSettingsScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.presentation.components.CustomBackButton
import com.artiusid.sdk.presentation.components.CustomButton
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    CustomBackButton(onClick = onNavigateBack)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Push Notifications
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Push Notifications",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable push notifications",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = uiState.pushNotificationsEnabled,
                            onCheckedChange = { viewModel.togglePushNotifications(it) }
                        )
                    }
                }
            }

            // Email Notifications
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Email Notifications",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable email notifications",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = uiState.emailNotificationsEnabled,
                            onCheckedChange = { viewModel.toggleEmailNotifications(it) }
                        )
                    }
                }
            }

            // Notification Types
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Notification Types",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Document verification updates",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = uiState.documentVerificationNotifications,
                                onCheckedChange = { viewModel.setDocumentVerificationNotifications(it) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account security alerts",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = uiState.securityAlerts,
                                onCheckedChange = { viewModel.setSecurityAlerts(it) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "System updates",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = uiState.systemUpdates,
                                onCheckedChange = { viewModel.setSystemUpdates(it) }
                            )
                        }
                    }
                }
            }

            // Notification Sound
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Notification Sound",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Play sound for notifications",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = uiState.notificationSound,
                            onCheckedChange = { viewModel.setNotificationSound(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            CustomButton(
                text = "Save Changes",
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 