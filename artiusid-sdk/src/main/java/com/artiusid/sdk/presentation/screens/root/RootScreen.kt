/*
 * File: RootScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.root

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.artiusid.sdk.data.model.AppNotificationState
import com.artiusid.sdk.navigation.AppNavigation
import com.artiusid.sdk.navigation.Screen

/**
 * Matches iOS RootView.swift exactly
 * Automatically switches between normal navigation and approval flow based on notification state
 */
@Composable
fun RootScreen(
    navController: NavHostController = rememberNavController()
) {
    // Watch notification state like iOS @EnvironmentObject var appNotificationState
    val notificationType by AppNotificationState.notificationType.collectAsState()
    
    // Switch views based on notification type (like iOS RootView)
    when (notificationType) {
        AppNotificationState.NotificationType.APPROVAL -> {
            // Navigate to ApprovalRequest screen automatically (like iOS)
            LaunchedEffect(notificationType) {
                navController.navigate(Screen.ApprovalRequest.route) {
                    // Clear back stack to prevent going back to normal flow
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                }
            }
            
            // Show the normal navigation (which will now show ApprovalRequest)
            AppNavigation(navController = navController)
        }
        
        AppNotificationState.NotificationType.DEFAULT -> {
            // Normal app navigation flow (like iOS default case)
            AppNavigation(navController = navController)
        }
    }
}