package com.artiusid.sdk.utils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Notification State Manager
 * Handles notification state similar to iOS AppNotificationState
 */
class NotificationStateManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: NotificationStateManager? = null
        
        val shared: NotificationStateManager
            get() = INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationStateManager().also { INSTANCE = it }
            }
    }
    
    enum class NotificationType {
        DEFAULT, APPROVAL
    }
    
    // Published state similar to iOS @Published properties
    var requestId by mutableStateOf<Int?>(null)
    var notificationType by mutableStateOf(NotificationType.DEFAULT)
    var notificationTitle by mutableStateOf("Approval Request")
    var notificationDescription by mutableStateOf("Test Approval Request: A request for \$0.00 has been requested.")
    
    fun handleApprovalNotification() {
        // Force a change first
        notificationType = NotificationType.DEFAULT
        
        // Then set to approval (similar to iOS implementation)
        notificationType = NotificationType.APPROVAL
    }
    
    fun reset() {
        notificationType = NotificationType.DEFAULT
        requestId = null
    }
}