/*
 * File: UrlConfiguration.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.config

/**
 * Configuration class for URL settings
 * This allows the sample app to pass environment and domain configuration to the SDK.
 * Optional iOS-style URL templates: when set, #env# and #domain# are replaced at runtime.
 */
data class UrlConfiguration(
    val environment: String = "Sandbox",
    val domain: String = "artiusid.dev",
    /** Optional. e.g. "https://#env#.#domain#" → mobile base. #env# = sandbox|dev|stage|"". */
    val urlTemplate: String? = null,
    /** Optional. e.g. "mobile.artiusid.dev". Used with urlTemplate. */
    val mobileDomain: String? = null,
    /** Optional. e.g. "https://#env#.#domain#" → registration base. */
    val registrationUrlTemplate: String? = null,
    /** Optional. e.g. "registration.artiusid.dev". */
    val registrationDomain: String? = null
) {
    companion object {
        // Available environments
        val AVAILABLE_ENVIRONMENTS = listOf(
            "Sandbox",
            "Development", 
            "QA",
            "Staging",
            "Production"
        )
        
        // Available domains
        val AVAILABLE_DOMAINS = listOf(
            "artiusid.dev",
            "artiusid.com",
            "artiusid.net", 
            "localhost:8080"
        )
        
        // Predefined configurations for easy use
        val SANDBOX_DEV = UrlConfiguration("Sandbox", "artiusid.dev")
        val DEVELOPMENT_DEV = UrlConfiguration("Development", "artiusid.dev")
        val STAGING_DEV = UrlConfiguration("Staging", "artiusid.dev")
        val PRODUCTION_COM = UrlConfiguration("Production", "artiusid.com")
        val LOCAL_TESTING = UrlConfiguration("Development", "localhost:8080")
    }
    
    /**
     * Validate the configuration.
     * If URL templates are provided, all four must be non-blank.
     */
    fun isValid(): Boolean {
        if (environment !in AVAILABLE_ENVIRONMENTS || domain.isBlank()) return false
        val useTemplates = !urlTemplate.isNullOrBlank() && !mobileDomain.isNullOrBlank() &&
            !registrationUrlTemplate.isNullOrBlank() && !registrationDomain.isNullOrBlank()
        if (useTemplates) return true
        if (urlTemplate.isNullOrBlank() && mobileDomain.isNullOrBlank() &&
            registrationUrlTemplate.isNullOrBlank() && registrationDomain.isNullOrBlank()) return true
        return false
    }
    
    /**
     * Get a readable description of this configuration
     */
    fun getDescription(): String {
        return "$environment.$domain"
    }
}
