package com.artiusid.sdk.models

/**
 * Represents head movement data for liveness detection
 */
data class HeadMovement(
    val yaw: Double = 0.0,
    val pitch: Double = 0.0,
    val roll: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
