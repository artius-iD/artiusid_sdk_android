package com.artiusid.sdk.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artiusid.sdk.ui.theme.SDKTheme

/**
 * Base activity for all SDK activities
 * Provides common functionality and theming
 */
abstract class BaseSDKActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "BaseSDKActivity"
        
        // Result codes
        const val RESULT_SUCCESS = Activity.RESULT_OK
        const val RESULT_CANCELLED = Activity.RESULT_CANCELED
        const val RESULT_ERROR = Activity.RESULT_FIRST_USER + 1
        
        // Extra keys
        const val EXTRA_RESULT_DATA = "result_data"
        const val EXTRA_ERROR_CODE = "error_code"
        const val EXTRA_ERROR_MESSAGE = "error_message"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d(TAG, "${this::class.simpleName} created")
        
        setContent {
            SDKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content()
                }
            }
        }
    }
    
    /**
     * Abstract method to be implemented by subclasses
     * This defines the main content of the activity
     */
    @Composable
    abstract fun Content()
    
    /**
     * Finish activity with success result
     */
    protected fun finishWithSuccess(resultData: Bundle? = null) {
        android.util.Log.d(TAG, "${this::class.simpleName} finishing with success")
        
        val intent = Intent().apply {
            resultData?.let { putExtras(it) }
        }
        
        setResult(RESULT_SUCCESS, intent)
        finish()
    }
    
    /**
     * Finish activity with error result
     */
    protected fun finishWithError(errorCode: Int, errorMessage: String, resultData: Bundle? = null) {
        android.util.Log.e(TAG, "${this::class.simpleName} finishing with error: $errorCode - $errorMessage")
        
        val intent = Intent().apply {
            putExtra(EXTRA_ERROR_CODE, errorCode)
            putExtra(EXTRA_ERROR_MESSAGE, errorMessage)
            resultData?.let { putExtras(it) }
        }
        
        setResult(RESULT_ERROR, intent)
        finish()
    }
    
    /**
     * Finish activity as cancelled
     */
    protected fun finishAsCancelled() {
        android.util.Log.d(TAG, "${this::class.simpleName} finishing as cancelled")
        
        setResult(RESULT_CANCELLED)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d(TAG, "${this::class.simpleName} destroyed")
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Handle back button press as cancellation
        finishAsCancelled()
    }
}
