/*
 * File: DocumentDetectionManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DocumentDetectionManager(private val context: Context) {
    private var isInitialized = false
    private var documentCorners: List<PointF>? = null
    private var documentDetected = false
    private var documentQuality = 0f
    private var onInitializationComplete: (() -> Unit)? = null
    private var initializationError: String? = null

    companion object {
        private const val TAG = "DocDetectManager"
        private const val MAX_INIT_ATTEMPTS = 10
        private const val INIT_DELAY_MS = 500L
    }

    init {
        // Initialize without OpenCV - using basic Android operations
        isInitialized = true
        Log.d(TAG, "DocumentDetectionManager initialized successfully")
    }

    suspend fun waitForInitialization(onComplete: () -> Unit) {
        if (isInitialized) {
            onComplete()
            return
        }

        var attempts = 0
        while (!isInitialized && attempts < MAX_INIT_ATTEMPTS) {
            kotlinx.coroutines.delay(INIT_DELAY_MS)
            attempts++
        }
        onComplete()
    }

    fun getInitializationError(): String? = initializationError

    @androidx.camera.core.ExperimentalGetImage
    fun createImageAnalyzer(
        onDocumentDetected: (List<PointF>, Float) -> Unit,
        onDocumentLost: () -> Unit
    ): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { imageAnalysis ->
                imageAnalysis.setAnalyzer(
                    java.util.concurrent.Executors.newSingleThreadExecutor()
                ) { imageProxy ->
                    processImage(imageProxy, onDocumentDetected, onDocumentLost)
                }
            }
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processImage(
        imageProxy: ImageProxy,
        onDocumentDetected: (List<PointF>, Float) -> Unit,
        onDocumentLost: () -> Unit
    ) {
        val image = imageProxy.image
        if (image != null) {
            val bitmap = imageToBitmap(image)
            val corners = detectDocumentCorners(bitmap)
            
            if (corners != null) {
                val quality = calculateDocumentQuality(bitmap, corners)
                documentCorners = corners
                documentDetected = true
                documentQuality = quality
                onDocumentDetected(corners, quality)
            } else {
                if (documentDetected) {
                    documentDetected = false
                    documentCorners = null
                    onDocumentLost()
                }
            }
        }
        imageProxy.close()
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, image.width, image.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun detectDocumentCorners(bitmap: Bitmap): List<PointF>? {
        // Simplified document detection without OpenCV
        // This is a basic implementation that looks for rectangular shapes
        // In a real implementation, you would use MLKit or other Android-compatible libraries
        
        val width = bitmap.width
        val height = bitmap.height
        
        // For now, return a simple rectangle covering most of the image
        // This is a placeholder implementation
        val margin = min(width, height) * 0.1f
        val corners = listOf(
            PointF(margin, margin),
            PointF(width - margin, margin),
            PointF(width - margin, height - margin),
            PointF(margin, height - margin)
        )
        
        return corners
    }

    private fun calculateDocumentQuality(bitmap: Bitmap, corners: List<PointF>): Float {
        var quality = 0f

        // Check if corners form a rectangle
        val angles = calculateAngles(corners)
        val isRectangular = angles.all { abs(it - 90) < 15 }
        if (isRectangular) quality += 0.4f

        // Check if document is centered
        val center = calculateCenter(corners)
        val imageCenter = PointF(bitmap.width / 2f, bitmap.height / 2f)
        val distance = calculateDistance(center, imageCenter)
        val maxDistance = min(bitmap.width, bitmap.height) / 4f
        if (distance < maxDistance) quality += 0.3f

        // Check if document is properly oriented
        val isOriented = checkOrientation(corners)
        if (isOriented) quality += 0.3f

        return quality
    }

    private fun calculateAngles(corners: List<PointF>): List<Double> {
        val angles = mutableListOf<Double>()
        for (i in corners.indices) {
            val p1 = corners[i]
            val p2 = corners[(i + 1) % 4]
            val p3 = corners[(i + 2) % 4]
            val angle = calculateAngle(p1, p2, p3)
            angles.add(angle)
        }
        return angles
    }

    private fun calculateAngle(p1: PointF, p2: PointF, p3: PointF): Double {
        val v1 = PointF(p1.x - p2.x, p1.y - p2.y)
        val v2 = PointF(p3.x - p2.x, p3.y - p2.y)
        val dot = v1.x * v2.x + v1.y * v2.y
        val mag1 = Math.sqrt((v1.x * v1.x + v1.y * v1.y).toDouble())
        val mag2 = Math.sqrt((v2.x * v2.x + v2.y * v2.y).toDouble())
        val cos = dot / (mag1 * mag2)
        return Math.toDegrees(Math.acos(cos.coerceIn(-1.0, 1.0)))
    }

    private fun calculateCenter(corners: List<PointF>): PointF {
        val x = corners.map { it.x }.average().toFloat()
        val y = corners.map { it.y }.average().toFloat()
        return PointF(x, y)
    }

    private fun checkOrientation(corners: List<PointF>): Boolean {
        // Check if the document is roughly horizontal
        val topEdge = calculateDistance(corners[0], corners[1])
        val bottomEdge = calculateDistance(corners[2], corners[3])
        val leftEdge = calculateDistance(corners[0], corners[3])
        val rightEdge = calculateDistance(corners[1], corners[2])

        val horizontalRatio = max(topEdge, bottomEdge) / min(topEdge, bottomEdge)
        val verticalRatio = max(leftEdge, rightEdge) / min(leftEdge, rightEdge)

        return horizontalRatio < 1.2 && verticalRatio < 1.2
    }

    private fun calculateDistance(p1: PointF, p2: PointF): Double {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
    }

    fun isInitialized(): Boolean = isInitialized
} 