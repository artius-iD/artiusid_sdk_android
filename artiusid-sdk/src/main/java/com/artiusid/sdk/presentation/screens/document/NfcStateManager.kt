/*
 * File: NfcStateManager.kt
 * Company: artius.iD, Inc.
 *
 * iOS parity (v2.0.43, v2.0.19): Central NFC state and retry guard so that:
 * - Only one NFC attempt can run at a time (prevents "resource unavailable" on retry).
 * - State can be reset when verification completes, user cancels, or navigates away.
 */

package com.artiusid.sdk.presentation.screens.document

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object NfcStateManager {
    private var isRetrying = false
    private val lock = ReentrantLock()

    /**
     * Try to start an NFC operation. Returns false if one is already in progress (caller should not start).
     */
    fun tryAcquire(): Boolean = lock.withLock {
        if (isRetrying) return false
        isRetrying = true
        true
    }

    /**
     * Release after NFC operation ends (success, failure, timeout, cancel).
     */
    fun release() {
        lock.withLock {
            isRetrying = false
        }
    }

    /**
     * Reset all NFC-related state. Call when verification completes, user cancels, or before starting NFC (e.g. entering chip scan).
     */
    fun resetNFCState() {
        release()
    }
}
