/*
 * File: VerificationDataHolder.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import com.artiusid.sdk.data.model.VerificationResultData

object VerificationDataHolder {
    private var _verificationData: VerificationResultData? = null
    
    fun setVerificationData(data: VerificationResultData?) {
        _verificationData = data
    }
    
    fun getVerificationData(): VerificationResultData? {
        return _verificationData
    }
    
    fun clearVerificationData() {
        _verificationData = null
    }
}
