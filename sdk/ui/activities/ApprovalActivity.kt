/*
 * File: ApprovalActivity.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 * 
 * Matches iOS ApprovalRequestView.swift exactly - handles approval flow with proper theming
 */

package com.artiusid.sdk.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.artiusid.sdk.data.api.ApiService
import com.artiusid.sdk.data.model.AppNotificationState
import com.artiusid.sdk.presentation.screens.approval.ApprovalRequestScreen
import com.artiusid.sdk.presentation.screens.approval.ApprovalRequestViewModel
import com.artiusid.sdk.presentation.screens.approval.ApprovalResponseScreen
import com.artiusid.sdk.ui.theme.EnhancedSDKTheme

/**
 * Approval Activity that handles approval notifications with proper SDK theming
 * Matches the pattern of AuthenticationActivity but for approval flow
 */
class ApprovalActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "ApprovalActivity"
        const val EXTRA_NOTIFICATION_TITLE = "notification_title"
        const val EXTRA_NOTIFICATION_DESCRIPTION = "notification_description"
        const val EXTRA_REQUEST_ID = "request_id"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d(TAG, "🚀 ApprovalActivity onCreate() called")
        
        try {
            // Get notification data from intent
            val notificationTitle = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE) ?: "Approval Request"
            val notificationDescription = intent.getStringExtra(EXTRA_NOTIFICATION_DESCRIPTION) ?: "Please review and approve this request"
            val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
            
            android.util.Log.d(TAG, "📋 Approval request - Title: $notificationTitle")
            android.util.Log.d(TAG, "📋 Approval request - Description: $notificationDescription")
            android.util.Log.d(TAG, "📋 Approval request - ID: $requestId")
            
            // Get theme configuration
            val themeConfig = com.artiusid.sdk.ui.theme.EnhancedThemeManager.getCurrentThemeConfig()
            android.util.Log.d(TAG, "📱 Theme config loaded: ${themeConfig.brandName}")
            
            // Set up notification state
            AppNotificationState.handleApprovalNotification(
                requestId = requestId,
                title = notificationTitle,
                description = notificationDescription
            )
            
            // Create ViewModel manually (without Hilt to avoid dependency issues)
            val viewModelFactory = object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ApprovalRequestViewModel::class.java)) {
                        // Create a mock ApiService instance for approval flow
                        val apiService = object : ApiService {
                            override suspend fun verify(clientId: Int, clientGroupId: Int, request: LinkedHashMap<String, Any>) = 
                                throw NotImplementedError("Not needed for approval flow")
                            override suspend fun authenticate(clientId: Int, clientGroupId: Int, accountNumber: String, request: com.artiusid.sdk.data.model.AuthenticationRequest) = 
                                throw NotImplementedError("Not needed for approval flow")
                            override suspend fun sendApprovalResponse(request: com.artiusid.sdk.data.model.ApprovalRequest) = 
                                throw NotImplementedError("Not needed for approval flow")
                            override suspend fun loadCertificate(clientId: Int, clientGroupId: Int, request: com.artiusid.sdk.data.model.LoadCertificateRequest) = 
                                throw NotImplementedError("Not needed for approval flow")
                            override suspend fun loadCertificate(request: com.artiusid.sdk.data.model.LoadCertificateRequest) = 
                                throw NotImplementedError("Not needed for approval flow")
                            override suspend fun sendApprovalRequestIOS(request: com.artiusid.sdk.data.model.ApprovalRequestTestingRequest) = 
                                throw NotImplementedError("Not needed for approval flow")
                            override suspend fun approval(request: com.artiusid.sdk.data.model.ApprovalRequest) = 
                                throw NotImplementedError("Not needed for approval flow")
                        }
                        @Suppress("UNCHECKED_CAST")
                        return ApprovalRequestViewModel(apiService) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
            val viewModel = ViewModelProvider(this, viewModelFactory)[ApprovalRequestViewModel::class.java]
            
            setContent {
                EnhancedSDKTheme(
                    themeConfig = themeConfig
                ) {
                    var currentScreen by remember { mutableStateOf("request") }
                    var approvalResponse by remember { mutableStateOf("") }
                    
                    when (currentScreen) {
                        "request" -> {
                            ApprovalRequestScreen(
                                onNavigateToApprovalResponse = { response ->
                                    approvalResponse = response
                                    currentScreen = "response"
                                },
                                onNavigateBack = {
                                    android.util.Log.d(TAG, "🔙 Back pressed - finishing approval activity")
                                    finish()
                                },
                                viewModel = viewModel
                            )
                        }
                        "response" -> {
                            ApprovalResponseScreen(
                                response = approvalResponse,
                                onNavigateToHome = {
                                    android.util.Log.d(TAG, "✅ Approval flow completed - finishing activity")
                                    finish()
                                }
                            )
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Error in ApprovalActivity onCreate", e)
            finish()
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        android.util.Log.d(TAG, "🔄 ApprovalActivity onNewIntent() - new approval request received")
        
        // Update the intent to the new one
        setIntent(intent)
        
        // Handle the new approval request (this will reset state)
        intent?.let { handleNewApprovalRequest(it) }
    }
    
    private fun handleNewApprovalRequest(intent: Intent) {
        android.util.Log.d(TAG, "🔄 Handling new approval request")
        
        // Get new notification data
        val notificationTitle = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE) ?: "Approval Request"
        val notificationDescription = intent.getStringExtra(EXTRA_NOTIFICATION_DESCRIPTION) ?: "Please review and approve this request"
        val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
        
        android.util.Log.d(TAG, "📋 New approval request - Title: $notificationTitle")
        android.util.Log.d(TAG, "📋 New approval request - Description: $notificationDescription")
        android.util.Log.d(TAG, "📋 New approval request - ID: $requestId")
        
        // Update notification state with new data
        AppNotificationState.handleApprovalNotification(
            requestId = requestId,
            title = notificationTitle,
            description = notificationDescription
        )
        
        // The Compose UI will automatically recompose with the new state
        // and the LaunchedEffect(Unit) will trigger a fresh authentication flow
    }

    override fun onBackPressed() {
        android.util.Log.d(TAG, "🔙 Back button pressed - finishing approval activity")
        super.onBackPressed()
    }
}
