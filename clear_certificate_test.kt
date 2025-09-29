// Test script to clear existing certificate
// Add this to BridgeMainActivity.kt as a temporary button or method

private fun clearExistingCertificate() {
    try {
        android.util.Log.d("BridgeMainActivity", "🧹 Clearing existing certificate...")
        
        // Method 1: Use APIManager to clear certificate and key
        val apiManager = com.artiusid.sdk.services.APIManager(this)
        apiManager.clearAndReloadIdentity()
        
        android.util.Log.d("BridgeMainActivity", "✅ Certificate cleared successfully")
        
        // Update UI to show certificate status
        checkCertificateStatus()
        
        lastResult = "✅ Certificate cleared successfully - ready for new registration"
        
    } catch (e: Exception) {
        android.util.Log.e("BridgeMainActivity", "❌ Error clearing certificate", e)
        lastResult = "❌ Error clearing certificate: ${e.message}"
    }
}
