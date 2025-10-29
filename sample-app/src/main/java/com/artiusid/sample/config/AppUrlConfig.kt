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
    // Options: "Sandbox", "Development", "Staging", "Production"
    // IMPORTANT: Must match certificate domains in api-cert-chain.pem
    // Available certificates:
    // - Sandbox: sandbox.mobile.artiusid.dev, sandbox.registration.artiusid.dev, sandbox.services.artiusid.dev
    // - Development: *.dev.artiusid.dev
    // - Staging: *.stage.artiusid.dev  
    // - Production: *.prod.artiusid.dev
    private const val ENVIRONMENT = "Sandbox"
    
    // Set your desired domain  
    // Options: "artiusid.dev", "artiusid.com", "artiusid.net", "localhost:8080"
    private const val DOMAIN = "artiusid.dev"
    
    /**
     * Get the configuration to pass to the SDK
     * 
     * For Sandbox environment, this will generate URLs like:
     * - https://sandbox.mobile.artiusid.dev/verifi/api/verification
     * - https://sandbox.registration.artiusid.dev/LoadCertificateFunction
     * - https://sandbox.services.artiusid.dev/ApprovalRequestTestingFunction
     * 
     * For other environments:
     * - Development: https://dev.mobile.artiusid.dev/...
     * - Staging: https://stage.mobile.artiusid.dev/...
     * - Production: https://prod.mobile.artiusid.dev/...
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
