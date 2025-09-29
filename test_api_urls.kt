// Test script to verify API URL construction
import com.artiusid.sdk.utils.UrlBuilder

fun main() {
    println("Testing API URL Construction:")
    println("=============================")
    
    // Mock context for testing
    val mockContext = MockContext()
    
    println("Certificate URL: ${UrlBuilder.getLoadCertificateUrl(mockContext)}")
    println("Verification URL: ${UrlBuilder.getVerificationUrl(mockContext)}")
    println("Authentication URL: ${UrlBuilder.getAuthenticationUrl(mockContext)}")
    println("Approval Request URL: ${UrlBuilder.getApprovalRequestUrl(mockContext)}")
    println("Approval Response URL: ${UrlBuilder.getApprovalResponseUrl(mockContext)}")
    
    println("\nBase URLs:")
    println("Certificate Base: ${UrlBuilder.getLoadCertificateBaseUrl(mockContext)}")
    println("Verification Base: ${UrlBuilder.getVerificationBaseUrl(mockContext)}")
    println("Approval Request Base: ${UrlBuilder.getApprovalRequestBaseUrl(mockContext)}")
    println("Approval Response Base: ${UrlBuilder.getApprovalResponseBaseUrl(mockContext)}")
}

class MockContext : android.content.Context() {
    override fun getSharedPreferences(name: String, mode: Int): android.content.SharedPreferences {
        return MockSharedPreferences()
    }
    // ... other required overrides would go here
}

class MockSharedPreferences : android.content.SharedPreferences {
    override fun getString(key: String, defValue: String?): String? {
        return "Staging" // Default environment
    }
    // ... other required overrides would go here
}
