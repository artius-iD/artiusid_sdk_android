/*
 * Okta OIDC configuration (matches iOS Okta.plist / AppConstants.Okta)
 * Used for browser sign-in to get Okta user ID and pass to ArtiusID SDK.
 */
package com.artiusid.sample.config

object OktaConfig {
    // Same values as iOS artiusid_ios_okta_mfa_app Okta.plist
    const val ISSUER = "https://integrator-6977887.okta.com"
    const val CLIENT_ID = "0oazutsn89PywJlLo697"
    /** Must be registered in Okta Application's Sign-in redirect URIs. Use this app's scheme so the redirect opens the app. */
    const val REDIRECT_URI = "com.artiusid.sampleapp:/callback"
    const val SCOPES = "openid profile offline_access okta.myAccount.appAuthenticator.manage okta.myAccount.appAuthenticator.read"

    val AUTHORIZE_URL: String get() = "$ISSUER/oauth2/v1/authorize"
    val TOKEN_URL: String get() = "$ISSUER/oauth2/v1/token"
}
