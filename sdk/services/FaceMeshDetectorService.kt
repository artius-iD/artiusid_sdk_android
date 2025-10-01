/*
 * File: FaceMeshDetectorService.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.services

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FaceMeshDetectorService {
    val faceResult: StateFlow<FaceMeshResult?>
    val segmentStatus: StateFlow<List<Boolean>>
    val currentInstruction: StateFlow<String>
    val isProcessingComplete: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    val calibrationCountdown: StateFlow<Int> // Countdown timer during calibration
    
    suspend fun detectFaceMesh(bitmap: Bitmap): FaceMeshResult
    
    fun startFaceDetection(): Flow<FaceMeshResult>
    
    fun stopFaceDetection()
    
    fun createImageAnalyzer(): ImageAnalysis
}

enum class ProcessingStage {
    INITIAL_INSTRUCTIONS,
    CAPTURE_PHOTO,
    CALIBRATING,
    SELFIE_CAPTURE,
    GUIDED_MESH_CAPTURE,
    BLINK_DETECTION, // Added for explicit blink detection
    COMPLETED
}

data class FaceMeshResult(
    val hasFace: Boolean,
    val confidence: Float,
    val landmarks: List<Point> = emptyList(),
    val error: String? = null,
    // iOS-like liveness detection properties
    val headYaw: Double = 0.0,
    val headPitch: Double = 0.0,
    val headRoll: Double = 0.0,
    val distanceToFace: Float = 0.0f,
    val blinkDetected: Boolean = false,
    val segmentIndex: Int = -1,
    val visitedSegments: List<Int> = emptyList(),
    val processingStage: ProcessingStage = ProcessingStage.INITIAL_INSTRUCTIONS,
    val instructionText: String = "",
    val hintText: String = "",
    val alignmentDirection: String = "",
    // Enhanced 3D Face Mesh properties (MediaPipe Face Landmarker)
    val faceLandmarks: List<FaceLandmark3D> = emptyList(), // 478 3D landmarks
    val blendshapes: List<Blendshape> = emptyList(), // 52 facial expression coefficients
    val faceTransformationMatrix: FloatArray? = null, // 4x4 transformation matrix
    val faceMeshVertices: List<Point3D> = emptyList(), // 3D mesh vertices
    val faceMeshTriangles: List<Triangle> = emptyList() // Face mesh triangulation
)

data class Point(
    val x: Float,
    val y: Float
)

// 3D point for advanced face mesh
data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
)

// 3D face landmark with visibility
data class FaceLandmark3D(
    val point: Point3D,
    val visibility: Float = 1.0f,
    val presence: Float = 1.0f
)

// Facial expression blendshape coefficient  
data class Blendshape(
    val categoryName: String,
    val score: Float
)

// Triangle for face mesh triangulation
data class Triangle(
    val vertexA: Int,
    val vertexB: Int, 
    val vertexC: Int
)

data class ScannedMesh(
    val segment: Int,
    val faceMeshData: String // JSON representation of face mesh
)

data class FaceCapture(
    val capturedImage: Bitmap?,
    val faceMeshData: String?,
    val scannedMeshes: List<ScannedMesh> = emptyList()
) 