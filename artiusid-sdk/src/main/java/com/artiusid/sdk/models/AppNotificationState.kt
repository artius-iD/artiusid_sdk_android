package com.artiusid.sdk.models

/**
 * Application notification state management
 */
object AppNotificationState {
    
    private var _isNotificationEnabled = true
    private var _currentNotification: String? = null
    
    val isNotificationEnabled: Boolean
        get() = _isNotificationEnabled
    
    val currentNotification: String?
        get() = _currentNotification
    
    fun enableNotifications() {
        _isNotificationEnabled = true
    }
    
    fun disableNotifications() {
        _isNotificationEnabled = false
    }
    
    fun setCurrentNotification(message: String?) {
        _currentNotification = message
    }
    
    fun clearNotification() {
        _currentNotification = null
    }
    
    fun handleApprovalNotification(requestId: Int, title: String, description: String) {
        _currentNotification = "Approval: $title - $description (ID: $requestId)"
    }
    
    fun reset() {
        _currentNotification = null
        _isNotificationEnabled = true
    }
}
