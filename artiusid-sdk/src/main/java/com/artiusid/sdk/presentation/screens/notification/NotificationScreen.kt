/*
 * File: NotificationScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.artiusid.sdk.presentation.components.AppTopBar
import java.util.Date
import java.text.SimpleDateFormat

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean
)

@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit
) {
    val notifications = remember {
        val now = Date()
        listOf(
            Notification(
                id = "1",
                title = "Document Verification",
                message = "Your document has been successfully verified.",
                timestamp = Date(now.time - 2 * 60 * 60 * 1000), // 2 hours ago
                isRead = true
            ),
            Notification(
                id = "2",
                title = "Face Verification",
                message = "Your face verification is pending. Please complete it soon.",
                timestamp = Date(now.time - 24 * 60 * 60 * 1000), // 1 day ago
                isRead = false
            ),
            Notification(
                id = "3",
                title = "Account Update",
                message = "Your account information has been updated successfully.",
                timestamp = Date(now.time - 2 * 24 * 60 * 60 * 1000), // 2 days ago
                isRead = true
            )
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Notifications",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification = notification)
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: Notification) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy HH:mm") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dateFormatter.format(notification.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 