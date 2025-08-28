package com.artiusid.sdk.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.artiusid.sdk.sdk.models.SDKError
import com.artiusid.sdk.sdk.ui.theme.ArtiusIDSDKTheme

/**
 * Base activity for all SDK activities
 */
abstract class BaseSDKActivity : ComponentActivity() {
    
    companion object {
        const val RESULT_SUCCESS = Activity.RESULT_OK
        const val RESULT_CANCELLED = Activity.RESULT_CANCELED
        const val RESULT_ERROR = Activity.RESULT_FIRST_USER
        
        const val EXTRA_ERROR_CODE = "extra_error_code"
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_RESULT_DATA = "extra_result_data"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ArtiusIDSDKTheme {
                Content()
            }
        }
    }
    
    /**
     * Content to be implemented by subclasses
     */
    @Composable
    abstract fun Content()
    
    /**
     * Finish activity with success result
     */
    protected fun finishWithSuccess(data: Any? = null) {
        val intent = Intent().apply {
            data?.let { putExtra(EXTRA_RESULT_DATA, it.toString()) }
        }
        setResult(RESULT_SUCCESS, intent)
        finish()
    }
    
    /**
     * Finish activity with error result
     */
    protected fun finishWithError(error: SDKError) {
        val intent = Intent().apply {
            putExtra(EXTRA_ERROR_CODE, error.code.name)
            putExtra(EXTRA_ERROR_MESSAGE, error.message)
        }
        setResult(RESULT_ERROR, intent)
        finish()
    }
    
    /**
     * Finish activity as cancelled
     */
    protected fun finishAsCancelled() {
        setResult(RESULT_CANCELLED)
        finish()
    }
}
