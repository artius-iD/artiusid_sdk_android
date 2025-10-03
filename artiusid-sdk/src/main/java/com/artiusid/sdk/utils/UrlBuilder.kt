/*
 * File: UrlBuilder.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.content.SharedPreferences
import com.artiusid.sdk.config.UrlConfiguration

object UrlBuilder {
    
    // Service paths matching iOS ServiceTypes
    private const val VERIFICATION_PATH = "verifi/api/verification"
    private const val AUTHENTICATION_PATH = "auth/api/auth"
    private const val APPROVAL_REQUEST_PATH = "ApprovalRequestTestingFunction"
    private const val APPROVAL_RESPONSE_PATH = "ApprovalResponseFunction"
    private const val LOAD_CERTIFICATE_PATH = "LoadCertificateFunction"
    
    // Current configuration - can be set by sample app
    private var currentConfiguration: UrlConfiguration? = null
    
    enum class Environment {
        SANDBOX, DEVELOPMENT, QA, STAGING, PRODUCTION
    }
    
    enum class ServiceType {
        VERIFICATION, AUTHENTICATION, APPROVAL_REQUEST, APPROVAL_RESPONSE, LOAD_CERTIFICATE
    }
    
    /**
     * Set URL configuration from sample app
     * This allows the sample app to pass a configuration file to the SDK
     */
    fun setConfiguration(configuration: UrlConfiguration) {
        if (configuration.isValid()) {
            currentConfiguration = configuration
            android.util.Log.d("UrlBuilder", "🔧 Configuration set: ${configuration.getDescription()}")
        } else {
            android.util.Log.e("UrlBuilder", "❌ Invalid configuration provided")
        }
    }
    
    /**
     * Get current configuration or create default
     */
    fun getCurrentConfiguration(): UrlConfiguration {
        return currentConfiguration ?: UrlConfiguration.SANDBOX_DEV
    }
    
    private fun getEnvironmentFromSettings(context: Context): Environment {
        val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val envString = prefs.getString("environment", "Sandbox") ?: "Sandbox"
        return when (envString) {
            "Sandbox" -> Environment.SANDBOX
            "Development" -> Environment.DEVELOPMENT
            "QA" -> Environment.QA
            "Staging" -> Environment.STAGING
            "Production" -> Environment.PRODUCTION
            else -> Environment.SANDBOX
        }
    }
    
    private fun getDomainFromSettings(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("domain", "artiusid.dev") ?: "artiusid.dev"
    }
    
    private fun getEnvironmentPrefix(environment: Environment): String {
        return when (environment) {
            Environment.SANDBOX -> "sandbox"
            Environment.DEVELOPMENT -> "dev"
            Environment.QA -> "qa"
            Environment.STAGING -> "stage"
            Environment.PRODUCTION -> "prod"
        }
    }
    
    private fun getBaseUrl(serviceType: ServiceType, @Suppress("UNUSED_PARAMETER") context: Context): String {
        val config = getCurrentConfiguration()
        val environment = stringToEnvironment(config.environment)
        val domain = config.domain
        val envPrefix = getEnvironmentPrefix(environment)
        
        return when (serviceType) {
            ServiceType.VERIFICATION, 
            ServiceType.AUTHENTICATION, 
            ServiceType.APPROVAL_REQUEST, 
            ServiceType.APPROVAL_RESPONSE -> "https://$envPrefix.mobile.$domain"
            ServiceType.LOAD_CERTIFICATE -> "https://$envPrefix.registration.$domain"
        }
    }
    
    private fun stringToEnvironment(envString: String): Environment {
        return when (envString) {
            "Sandbox" -> Environment.SANDBOX
            "Development" -> Environment.DEVELOPMENT
            "QA" -> Environment.QA
            "Staging" -> Environment.STAGING
            "Production" -> Environment.PRODUCTION
            else -> Environment.SANDBOX
        }
    }
    
    private fun getServicePath(serviceType: ServiceType): String {
        return when (serviceType) {
            ServiceType.VERIFICATION -> VERIFICATION_PATH
            ServiceType.AUTHENTICATION -> AUTHENTICATION_PATH
            ServiceType.APPROVAL_REQUEST -> APPROVAL_REQUEST_PATH
            ServiceType.APPROVAL_RESPONSE -> APPROVAL_RESPONSE_PATH
            ServiceType.LOAD_CERTIFICATE -> LOAD_CERTIFICATE_PATH
        }
    }
    
    fun buildEndpointUrl(context: Context, serviceType: ServiceType): String {
        val baseUrl = getBaseUrl(serviceType, context)
        val path = getServicePath(serviceType)
        val fullUrl = "$baseUrl/$path"
        android.util.Log.d("UrlBuilder", "🌐 Built endpoint URL for $serviceType: $fullUrl")
        return fullUrl
    }
    
    fun buildBaseUrl(context: Context, serviceType: ServiceType): String {
        val baseUrl = getBaseUrl(serviceType, context)
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
    
    // Configuration helper functions for sample app
    fun setEnvironment(context: Context, environment: String) {
        val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("environment", environment).apply()
        android.util.Log.d("UrlBuilder", "🔧 Environment set to: $environment")
    }
    
    fun setDomain(context: Context, domain: String) {
        val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("domain", domain).apply()
        android.util.Log.d("UrlBuilder", "🌐 Domain set to: $domain")
    }
    
    fun getCurrentEnvironment(context: Context): String {
        return getEnvironmentFromSettings(context).name
    }
    
    fun getCurrentDomain(context: Context): String {
        return getDomainFromSettings(context)
    }
    
    fun getAvailableEnvironments(): List<String> {
        return Environment.values().map { it.name }
    }
    
    // Get current configuration as a readable string
    fun getCurrentConfiguration(context: Context): String {
        val env = getCurrentEnvironment(context)
        val domain = getCurrentDomain(context)
        return "$env.$domain"
    }
} 