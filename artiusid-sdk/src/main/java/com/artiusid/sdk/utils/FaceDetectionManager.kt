/*
 * File: FaceDetectionManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FaceDetectionManager {
    private val faceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
        
        FaceDetection.getClient(options)
    }

    suspend fun detectFaces(bitmap: Bitmap): List<Face> = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                continuation.resume(faces)
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    fun getFaceQualityScore(face: Face): Float {
        var score = 0f
        
        // Check if eyes are open
        face.leftEyeOpenProbability?.let { score += it }
        face.rightEyeOpenProbability?.let { score += it }
        
        // Check if smiling
        face.smilingProbability?.let { score += it }
        
        // Check if face is looking at camera - using stricter thresholds for straight-forward detection
        face.headEulerAngleX?.let { 
            if (it in -8f..8f) score += 1f 
        }
        face.headEulerAngleY?.let { 
            if (it in -8f..8f) score += 1f 
        }
        
        // Normalize score to 0-1 range
        return (score / 5f).coerceIn(0f, 1f)
    }

    fun getFaceBoundingBox(face: Face): Rect {
        return face.boundingBox
    }

    fun hasRequiredLandmarks(face: Face): Boolean {
        return face.getLandmark(FaceLandmark.LEFT_EYE) != null &&
               face.getLandmark(FaceLandmark.RIGHT_EYE) != null &&
               face.getLandmark(FaceLandmark.NOSE_BASE) != null &&
               face.getLandmark(FaceLandmark.MOUTH_BOTTOM) != null
    }
} 