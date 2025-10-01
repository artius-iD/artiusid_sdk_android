/*
 * File: SettingsUiState.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.settings

data class SettingsUiState(
    val environment: String = "Staging",
    val environmentOptions: List<String> = listOf("Development", "QA", "Staging", "Production"),
    val isCertCleared: Boolean = false,
    val isCertClearing: Boolean = false,

    val isDeveloperMode: Boolean = false,
    val isDemoMode: Boolean = false,
    val enableS3: Boolean = false,

    val displayImageOverlays: Boolean = false,
    val displayDocumentOutline: Boolean = false,
    val displayTargetObjectOutline: Boolean = false,

    val isApprovalLoading: Boolean = false,
    val isApprovalSuccess: Boolean = false,
    val approvalResultMessage: String = "",

    val deviceId: String = "",
    val clientId: String = "1",
    val clientGroupId: String = "1",
    val accountNumber: String = "",

    val logLevel: String = "Debug",
    val logLevelOptions: List<String> = listOf("Debug", "Info", "Warning", "Error"),
    val logLevelDescription: String = "Records all log messages: Debug, Info, Warning, and Error",

    val logs: List<String> = emptyList(),
    val logCount: Int = 0
) 