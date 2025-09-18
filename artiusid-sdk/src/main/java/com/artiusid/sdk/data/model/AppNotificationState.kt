/*
 * File: AppNotificationState.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Matches iOS AppNotificationState.swift
 * Manages approval request notification state
 */
object AppNotificationState {
    
    enum class NotificationType {
        DEFAULT, APPROVAL
    }
    
    private val _requestId = MutableStateFlow<Int?>(null)
    val requestId: StateFlow<Int?> = _requestId.asStateFlow()
    
    private val _notificationType = MutableStateFlow(NotificationType.DEFAULT)
    val notificationType: StateFlow<NotificationType> = _notificationType.asStateFlow()
    
    private val _notificationTitle = MutableStateFlow("Approval Request")
    val notificationTitle: StateFlow<String> = _notificationTitle.asStateFlow()
    
    private val _notificationDescription = MutableStateFlow("Test Approval Request: A request for \$0.00 has been requested.")
    val notificationDescription: StateFlow<String> = _notificationDescription.asStateFlow()
    
    fun handleApprovalNotification(requestId: Int? = null, title: String? = null, description: String? = null) {
        // Set notification data like iOS AppDelegate.handleNotification
        if (!title.isNullOrEmpty()) {
            _notificationTitle.value = title
        }
        if (!description.isNullOrEmpty()) {
            _notificationDescription.value = description
        }
        
        // Force a change first like iOS
        _notificationType.value = NotificationType.DEFAULT
        _requestId.value = requestId
        
        // Next run loop: .approval (like iOS DispatchQueue.main.async)
        _notificationType.value = NotificationType.APPROVAL
    }
    
    fun reset() {
        _notificationType.value = NotificationType.DEFAULT
        _requestId.value = null
    }
} 