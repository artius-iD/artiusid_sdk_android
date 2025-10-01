/*
 * File: LogManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.repository

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Matches iOS LogManager functionality
 */
object LogManager {
    private val logs = mutableListOf<LogEntry>()
    private const val TAG = "LogManagerDebug"
    private const val MAX_LOGS = 500
    
    enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
    
    data class LogEntry(
        val timestamp: String,
        val level: LogLevel,
        val source: String,
        val message: String
    ) {
        override fun toString(): String {
            return "[$timestamp] [${level.name}] [$source] $message"
        }
    }
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    
    fun getLogs(): List<String> {
        Log.d(TAG, "getLogs called, logs size: ${logs.size}")
        return logs.map { it.toString() }
    }
    
    fun addLog(message: String, level: LogLevel = LogLevel.INFO, source: String = "App") {
        val timestamp = dateFormatter.format(Date())
        val entry = LogEntry(timestamp, level, source, message)
        
        synchronized(logs) {
            logs.add(entry)
            if (logs.size > MAX_LOGS) {
                logs.removeAt(0)
            }
        }
        
        Log.d(TAG, "addLog: $entry")
    }
    
    fun clearLogs() {
        synchronized(logs) {
            logs.clear()
        }
        Log.d(TAG, "clearLogs called")
    }
    
    fun exportLogs(): String {
        val header = """
            ArtiusID Debug Logs
            ===================
            Export Time: ${dateFormatter.format(Date())}
            Total Logs: ${logs.size}
            
            Device Info:
            - Build: ${android.os.Build.MODEL} (${android.os.Build.DEVICE})
            - Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})
            
            Logs:
            -----
            
        """.trimIndent()
        
        return if (logs.isNotEmpty()) {
            header + logs.joinToString("\n") { it.toString() }
        } else {
            header + "No logs available."
        }
    }
    
    // Convenience methods matching iOS usage
    fun logDebug(message: String, source: String = "App") {
        addLog(message, LogLevel.DEBUG, source)
    }
    
    fun logInfo(message: String, source: String = "App") {
        addLog(message, LogLevel.INFO, source)
    }
    
    fun logWarning(message: String, source: String = "App") {
        addLog(message, LogLevel.WARNING, source)
    }
    
    fun logError(message: String, source: String = "App") {
        addLog(message, LogLevel.ERROR, source)
    }
} 