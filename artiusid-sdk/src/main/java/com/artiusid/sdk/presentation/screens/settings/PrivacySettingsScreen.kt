/*
 * File: PrivacySettingsScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    navController: NavController,
    viewModel: PrivacySettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
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
            // Data Collection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Data Collection",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Control what data we collect and how we use it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = uiState.dataCollectionEnabled,
                        onCheckedChange = { viewModel.setDataCollection(it) },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Location Services
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Location Services",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Allow app to access your location",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = uiState.locationServicesEnabled,
                        onCheckedChange = { viewModel.setLocationServices(it) },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Analytics
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Help us improve by sharing usage data",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = uiState.analyticsEnabled,
                        onCheckedChange = { viewModel.setAnalytics(it) },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Personalized Ads
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Personalized Ads",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Show ads based on your interests",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = uiState.personalizedAdsEnabled,
                        onCheckedChange = { viewModel.setPersonalizedAds(it) },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
} 