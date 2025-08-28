package com.artiusid.utils

import android.content.Context
import android.content.SharedPreferences

object UrlBuilder {
    
    // Base URLs matching iOS AppConstants
    private const val VERIFICATION_BASE_URL = "https://service-mobile.#env#artiusid.dev"
    private const val AUTHENTICATION_BASE_URL = "https://service-mobile.#env#artiusid.dev"
    private const val APPROVAL_RESPONSE_URL = "https://service-mobile.#env#artiusid.dev"
    private const val APPROVAL_REQUEST_URL = "https://service-mobile.#env#artiusid.dev"
    private const val LOAD_CERTIFICATE_URL = "https://service-registration.#env#artiusid.dev"
    
    // Service paths matching iOS ServiceTypes
    private const val VERIFICATION_PATH = "verifi/api/verification"
    private const val AUTHENTICATION_PATH = "auth/api/auth"
    private const val APPROVAL_REQUEST_PATH = "ApprovalRequestTestingFunction"
    private const val APPROVAL_RESPONSE_PATH = "ApprovalResponseFunction"
    private const val LOAD_CERTIFICATE_PATH = "LoadCertificateFunction"
    
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
        val baseUrl = when (serviceType) {
            ServiceType.VERIFICATION -> VERIFICATION_BASE_URL
            ServiceType.AUTHENTICATION -> AUTHENTICATION_BASE_URL
            ServiceType.APPROVAL_REQUEST -> APPROVAL_REQUEST_URL
            ServiceType.APPROVAL_RESPONSE -> APPROVAL_RESPONSE_URL
            ServiceType.LOAD_CERTIFICATE -> LOAD_CERTIFICATE_URL
        }
        
        val envDomain = getEnvironmentDomain(environment)
        return baseUrl.replace("#env#", envDomain)
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
        val environment = getEnvironmentFromSettings(context)
        val baseUrl = getBaseUrl(serviceType, environment)
        val path = getServicePath(serviceType)
        return "$baseUrl/$path"
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