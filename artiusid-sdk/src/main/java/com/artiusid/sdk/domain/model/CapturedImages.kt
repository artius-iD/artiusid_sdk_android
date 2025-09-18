/*
 * File: CapturedImages.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

//
// CapturedImages.kt
// artiusid
//
// Author: Todd Bryant
// Company: artius.iD
//

package com.artiusid.sdk.domain.model

import android.graphics.Bitmap

data class CapturedImages(
    val frontImage: Bitmap? = null,
    val backImage: Bitmap? = null,
    val faceImage: Bitmap? = null,
    val passportImage: Bitmap? = null
) 