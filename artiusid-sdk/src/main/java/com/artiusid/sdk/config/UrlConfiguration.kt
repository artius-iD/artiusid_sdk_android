/*
 * File: UrlConfiguration.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.config

/**
 * Configuration class for URL settings
 * This allows the sample app to pass environment and domain configuration to the SDK
 */
data class UrlConfiguration(
    val environment: String = "Sandbox",
    val domain: String = "artiusid.dev"
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
     * Validate the configuration
     */
    fun isValid(): Boolean {
        return environment in AVAILABLE_ENVIRONMENTS && domain.isNotBlank()
    }
    
    /**
     * Get a readable description of this configuration
     */
    fun getDescription(): String {
        return "$environment.$domain"
    }
}
