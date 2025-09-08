package com.artiusid.sdk.models

/**
 * Verification results container - EXACT STANDALONE MATCH
 */
object VerificationResults {
    
    enum class Status {
        SUCCESS,
        FAILURE,
        PENDING,
        CANCELLED,
        ERROR
    }
    
    data class Result(
        val status: Status,
        val message: String? = null,
        val data: Any? = null,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    // Static result instances for common cases
    val SUCCESS = Result(Status.SUCCESS, "Verification completed successfully")
    val FAILURE = Result(Status.FAILURE, "Verification failed")
    val CANCELLED = Result(Status.CANCELLED, "Verification cancelled by user")
    val ERROR = Result(Status.ERROR, "An error occurred during verification")
    
    fun success(message: String? = null, data: Any? = null) = Result(Status.SUCCESS, message, data)
    fun failure(message: String? = null) = Result(Status.FAILURE, message)
    fun error(message: String? = null) = Result(Status.ERROR, message)
}
