/*
 * File: AppUrlConfig.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample.config

import com.artiusid.sdk.config.UrlConfiguration

/**
 * Sample App URL Configuration
 * 
 * Edit this file to change the environment and domain settings for the SDK.
 * The sample app will pass this configuration to the UrlBuilder.
 */
object AppUrlConfig {
    
    /**
     * EDIT THESE VALUES TO CONFIGURE YOUR ENVIRONMENT
     */
    
    // Set your desired environment
    // Options: "Sandbox", "Development", "QA", "Staging", "Production"
    private const val ENVIRONMENT = "Sandbox"
    
    // Set your desired domain  
    // Options: "artiusid.dev", "artiusid.com", "artiusid.net", "localhost:8080"
    private const val DOMAIN = "artiusid.dev"
    
    /**
     * Get the configuration to pass to the SDK
     * This will generate URLs like:
     * - https://sandbox.mobile.artiusid.dev/verifi/api/verification
     * - https://sandbox.registration.artiusid.dev/LoadCertificateFunction
     */
    fun getConfiguration(): UrlConfiguration {
        return UrlConfiguration(
            environment = ENVIRONMENT,
            domain = DOMAIN
        )
    }
    
    /**
     * Alternative predefined configurations you can use instead:
     * 
     * For local development:
     * return UrlConfiguration.LOCAL_TESTING
     * 
     * For sandbox testing:
     * return UrlConfiguration.SANDBOX_DEV
     * 
     * For staging:
     * return UrlConfiguration.STAGING_DEV
     * 
     * For production:
     * return UrlConfiguration.PRODUCTION_COM
     */
}
