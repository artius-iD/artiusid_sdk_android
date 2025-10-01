/*
 * File: UrlBuilder.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.content.SharedPreferences

object UrlBuilder {
    
    enum class Environment {
        DEVELOPMENT, QA, STAGING, PRODUCTION
    }
    
    enum class ServiceType {
        VERIFICATION, AUTHENTICATION, APPROVAL_REQUEST, APPROVAL_RESPONSE, LOAD_CERTIFICATE
    }
    
    private fun getEnvironmentFromSettings(context: Context): Environment {
        val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val envString = prefs.getString("environment", "Staging") ?: "Staging"
        return when (envString) {
            "Development" -> Environment.DEVELOPMENT
            "QA" -> Environment.QA
            "Staging" -> Environment.STAGING
            "Production" -> Environment.PRODUCTION
            else -> Environment.STAGING
        }
    }
    
    private fun getEnvironmentDomain(environment: Environment): String {
        return when (environment) {
            Environment.DEVELOPMENT -> "dev."
            Environment.QA -> "qa."
            Environment.STAGING -> "stage."
            Environment.PRODUCTION -> ""
        }
    }
    
    private fun getBaseUrl(serviceType: ServiceType, environment: Environment): String {
        // Use the new sandbox endpoints (environment parameter kept for future extensibility)
        return when (serviceType) {
            ServiceType.VERIFICATION -> "https://sandbox.mobile.artiusid.dev"
            ServiceType.AUTHENTICATION -> "https://sandbox.mobile.artiusid.dev"
            ServiceType.APPROVAL_REQUEST -> "https://sandbox.mobile.artiusid.dev"
            ServiceType.APPROVAL_RESPONSE -> "https://sandbox.mobile.artiusid.dev"
            ServiceType.LOAD_CERTIFICATE -> "https://sandbox.registration.artiusid.dev"
        }
    }
    
    private fun getServicePath(serviceType: ServiceType): String {
        return when (serviceType) {
            ServiceType.VERIFICATION -> "verifi/api/verification"
            ServiceType.AUTHENTICATION -> "auth/api/auth"
            ServiceType.APPROVAL_REQUEST -> "ApprovalRequestTestingFunction"
            ServiceType.APPROVAL_RESPONSE -> "ApprovalResponseFunction"
            ServiceType.LOAD_CERTIFICATE -> "LoadCertificateFunction"
        }
    }
    
    fun buildEndpointUrl(context: Context, serviceType: ServiceType): String {
        val environment = getEnvironmentFromSettings(context)
        val baseUrl = getBaseUrl(serviceType, environment)
        val path = getServicePath(serviceType)
        val fullUrl = "$baseUrl/$path"
        android.util.Log.d("UrlBuilder", "🌐 Built endpoint URL for $serviceType: $fullUrl")
        return fullUrl
    }
    
    fun buildBaseUrl(context: Context, serviceType: ServiceType): String {
        val environment = getEnvironmentFromSettings(context)
        val baseUrl = getBaseUrl(serviceType, environment)
        return "$baseUrl/"
    }
    
    // Convenience methods for specific services
    fun getVerificationUrl(context: Context): String = buildEndpointUrl(context, ServiceType.VERIFICATION)
    fun getAuthenticationUrl(context: Context): String = buildEndpointUrl(context, ServiceType.AUTHENTICATION)
    fun getApprovalRequestUrl(context: Context): String = buildEndpointUrl(context, ServiceType.APPROVAL_REQUEST)
    fun getApprovalResponseUrl(context: Context): String = buildEndpointUrl(context, ServiceType.APPROVAL_RESPONSE)
    fun getLoadCertificateUrl(context: Context): String = buildEndpointUrl(context, ServiceType.LOAD_CERTIFICATE)
    
    // Base URLs for Retrofit
    fun getVerificationBaseUrl(context: Context): String = buildBaseUrl(context, ServiceType.VERIFICATION)
    fun getApprovalRequestBaseUrl(context: Context): String = buildBaseUrl(context, ServiceType.APPROVAL_REQUEST)
    fun getApprovalResponseBaseUrl(context: Context): String = buildBaseUrl(context, ServiceType.APPROVAL_RESPONSE)
    fun getLoadCertificateBaseUrl(context: Context): String = buildBaseUrl(context, ServiceType.LOAD_CERTIFICATE)

    fun getVerificationStatusUrl(context: Context, verificationId: String): String {
        val base = getVerificationUrl(context)
        return "$base/status/$verificationId"
    }

    fun getVerificationHistoryUrl(context: Context): String {
        val base = getVerificationUrl(context)
        return "$base/history"
    }
} 