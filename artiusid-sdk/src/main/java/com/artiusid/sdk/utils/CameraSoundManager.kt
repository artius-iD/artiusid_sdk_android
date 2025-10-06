/*
 * File: CameraSoundManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages camera capture sound effects
 * Provides consistent audio feedback for photo capture events
 */
class CameraSoundManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CameraSoundManager"
        private const val CAPTURE_TONE_DURATION = 200 // milliseconds
    }
    
    private var toneGenerator: ToneGenerator? = null
    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    
    init {
        try {
            // Initialize tone generator for camera sounds with higher volume
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            
            // Initialize SoundPool for system sounds
            soundPool = SoundPool.Builder()
                .setMaxStreams(1)
                .build()
                
            Log.d(TAG, "Camera sound manager initialized with multiple sound options")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize sound systems: ${e.message}")
        }
    }
    
    /**
     * Play camera capture sound
     * Uses audible notification tone
     */
    suspend fun playCaptureSound() {
        withContext(Dispatchers.IO) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, CAPTURE_TONE_DURATION)
                Log.d(TAG, "📸🔊 Camera capture sound played")
            } catch (e: Exception) {
                try {
                    // Fallback to DTMF tone
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_1, CAPTURE_TONE_DURATION)
                    Log.d(TAG, "📸🔊 Camera capture sound played (fallback)")
                } catch (e2: Exception) {
                    Log.w(TAG, "Failed to play capture sound: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Play success sound for successful capture
     * Uses a more pleasant approval chime
     */
    suspend fun playSuccessSound() {
        withContext(Dispatchers.IO) {
            try {
                // Use a pleasant success tone
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, CAPTURE_TONE_DURATION)
                Log.d(TAG, "✅🔊 Success approval chime played")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to play success sound: ${e.message}")
            }
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            toneGenerator?.release()
            toneGenerator = null
            
            soundPool?.release()
            soundPool = null
            
            mediaPlayer?.release()
            mediaPlayer = null
            
            Log.d(TAG, "Camera sound manager cleaned up")
        } catch (e: Exception) {
            Log.w(TAG, "Error during cleanup: ${e.message}")
        }
    }
}
