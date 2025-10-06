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
     * Tries multiple sound options for maximum audibility
     */
    suspend fun playCaptureSound() {
        withContext(Dispatchers.IO) {
            try {
                // Try multiple sound options for better audibility
                var soundPlayed = false
                
                // Option 1: Try notification sound (more audible)
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, CAPTURE_TONE_DURATION)
                    soundPlayed = true
                    Log.d(TAG, "📸🔊 Camera capture sound played (ACK tone)")
                } catch (e: Exception) {
                    Log.w(TAG, "ACK tone failed: ${e.message}")
                }
                
                // Option 2: Fallback to beep if ACK failed
                if (!soundPlayed) {
                    try {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, CAPTURE_TONE_DURATION)
                        soundPlayed = true
                        Log.d(TAG, "📸🔊 Camera capture sound played (BEEP tone)")
                    } catch (e: Exception) {
                        Log.w(TAG, "BEEP tone failed: ${e.message}")
                    }
                }
                
                // Option 3: Try DTMF tone as final fallback
                if (!soundPlayed) {
                    try {
                        toneGenerator?.startTone(ToneGenerator.TONE_DTMF_1, CAPTURE_TONE_DURATION)
                        Log.d(TAG, "📸🔊 Camera capture sound played (DTMF tone)")
                    } catch (e: Exception) {
                        Log.w(TAG, "All sound options failed: ${e.message}")
                    }
                }
                
            } catch (e: Exception) {
                Log.w(TAG, "Failed to play capture sound: ${e.message}")
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
