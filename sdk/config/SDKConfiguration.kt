/*
 * File: SDKConfiguration.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.artiusid.sdk.models.SDKImageOverrides

/**
 * SDK Configuration for the bridge to standalone application
 */
@Parcelize
data class SDKConfiguration(
    val apiKey: String,
    val baseUrl: String = "https://api.artiusid.com",
    val environment: Environment = Environment.PRODUCTION,
    val enableLogging: Boolean = false,
    val enableAnalytics: Boolean = true,
    val enableBiometrics: Boolean = true,
    val enableNFC: Boolean = true,
    val timeoutSeconds: Int = 30,
    // Host app context sharing for mTLS and Firebase
    val hostAppPackageName: String? = null,
    val sharedCertificateContext: Boolean = true,
    val sharedFirebaseContext: Boolean = true,
    // Localization support
    val localizationOverrides: Map<String, String> = emptyMap(),
    // Image and GIF override support
    val imageOverrides: SDKImageOverrides = SDKImageOverrides()
) : Parcelable

enum class Environment {
    DEVELOPMENT,
    STAGING, 
    PRODUCTION
}