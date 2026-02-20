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
        SANDBOX, DEVELOPMENT, STAGING, PRODUCTION
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
        // 🚨 CRITICAL FIX: Use host app context if available to ensure SharedPreferences consistency
        val actualContext = try {
            com.artiusid.sdk.ArtiusIDSDK.getHostAppContext() ?: context
        } catch (e: Exception) {
            android.util.Log.w("UrlBuilder", "Could not get host app context, using provided context: ${e.message}")
            context
        }
        
        val prefs: SharedPreferences = actualContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val envString = prefs.getString("environment", "Sandbox") ?: "Sandbox"
        
        android.util.Log.d("UrlBuilder", "🚨 CONTEXT DEBUG: Using context: ${actualContext.javaClass.simpleName}")
        android.util.Log.d("UrlBuilder", "🚨 CONTEXT DEBUG: Package name: ${actualContext.packageName}")
        android.util.Log.d("UrlBuilder", "🚨 CONTEXT DEBUG: Environment from SharedPreferences: $envString")
        
        return when (envString) {
            "Sandbox" -> Environment.SANDBOX
            "Development" -> Environment.DEVELOPMENT
            "Staging" -> Environment.STAGING
            "Production" -> Environment.PRODUCTION
            else -> Environment.SANDBOX
        }
    }
    
    private fun getDomainFromSettings(context: Context): String {
        // 🚨 CRITICAL FIX: Use host app context if available to ensure SharedPreferences consistency
        val actualContext = try {
            com.artiusid.sdk.ArtiusIDSDK.getHostAppContext() ?: context
        } catch (e: Exception) {
            android.util.Log.w("UrlBuilder", "Could not get host app context, using provided context: ${e.message}")
            context
        }
        
        val prefs: SharedPreferences = actualContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("domain", "artiusid.dev") ?: "artiusid.dev"
    }
    
    /** Token for #env# in URL templates (iOS parity: empty for production). */
    private fun getEnvironmentToken(environment: Environment): String {
        return when (environment) {
            Environment.SANDBOX -> "sandbox"
            Environment.DEVELOPMENT -> "dev"
            Environment.STAGING -> "stage"
            Environment.PRODUCTION -> ""
        }
    }
    
    private fun getEnvironmentPrefix(environment: Environment): String {
        return when (environment) {
            Environment.SANDBOX -> "sandbox"
            Environment.DEVELOPMENT -> "service"
            Environment.STAGING -> "service"
            Environment.PRODUCTION -> ""
        }
    }
    
    private fun getEnvironmentSuffix(environment: Environment): String {
        return when (environment) {
            Environment.SANDBOX -> ""
            Environment.DEVELOPMENT -> ".dev"
            Environment.STAGING -> ".stage"
            Environment.PRODUCTION -> ""
        }
    }
    
    private fun getBaseUrl(serviceType: ServiceType, context: Context): String {
        val environment = getEnvironmentFromSettings(context)
        val config = currentConfiguration
        val envToken = getEnvironmentToken(environment)
        
        // iOS-style URL templates: if all four are set, build URLs from templates
        if (config != null && !config.urlTemplate.isNullOrBlank() && !config.mobileDomain.isNullOrBlank() &&
            !config.registrationUrlTemplate.isNullOrBlank() && !config.registrationDomain.isNullOrBlank()) {
            val mobileBase = config.urlTemplate!!
                .replace("#env#", envToken)
                .replace("#domain#", config.mobileDomain!!)
            val registrationBase = config.registrationUrlTemplate!!
                .replace("#env#", envToken)
                .replace("#domain#", config.registrationDomain!!)
            return when (serviceType) {
                ServiceType.VERIFICATION, ServiceType.AUTHENTICATION -> mobileBase
                ServiceType.APPROVAL_REQUEST, ServiceType.APPROVAL_RESPONSE -> mobileBase
                ServiceType.LOAD_CERTIFICATE -> registrationBase
            }
        }
        
        val domain = getDomainFromSettings(context)
        val envPrefix = getEnvironmentPrefix(environment)
        val envSuffix = getEnvironmentSuffix(environment)
        
        android.util.Log.d("UrlBuilder", "getBaseUrl for $serviceType env=$environment domain=$domain")
        
        return when (serviceType) {
            ServiceType.VERIFICATION,
            ServiceType.AUTHENTICATION -> {
                when (environment) {
                    Environment.SANDBOX -> "https://$envPrefix.mobile.$domain"
                    Environment.DEVELOPMENT, Environment.STAGING ->
                        "https://$envPrefix-mobile$envSuffix.$domain"
                    Environment.PRODUCTION -> "https://mobile.$domain"
                }
            }
            ServiceType.APPROVAL_REQUEST,
            ServiceType.APPROVAL_RESPONSE -> {
                when (environment) {
                    Environment.SANDBOX -> "https://$envPrefix.services.$domain"
                    Environment.DEVELOPMENT, Environment.STAGING ->
                        "https://$envPrefix-mobile$envSuffix.$domain"
                    Environment.PRODUCTION -> "https://mobile.$domain"
                }
            }
            ServiceType.LOAD_CERTIFICATE -> {
                when (environment) {
                    Environment.SANDBOX -> "https://$envPrefix.registration.$domain"
                    Environment.DEVELOPMENT, Environment.STAGING ->
                        "https://$envPrefix-registration$envSuffix.$domain"
                    Environment.PRODUCTION -> "https://registration.$domain"
                }
            }
        }
    }
    
    private fun stringToEnvironment(envString: String): Environment {
        return when (envString) {
            "Sandbox" -> Environment.SANDBOX
            "Development" -> Environment.DEVELOPMENT
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
        
        // ✅ FIX: Also update the currentConfiguration object
        val currentDomain = getDomainFromSettings(context)
        currentConfiguration = UrlConfiguration(
            environment = environment,
            domain = currentDomain
        )
        
        android.util.Log.d("UrlBuilder", "🔧 Environment set to: $environment")
        android.util.Log.d("UrlBuilder", "🔧 Updated currentConfiguration: ${currentConfiguration?.getDescription()}")
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