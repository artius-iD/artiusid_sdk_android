/*
 * File: NfcHandler.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcHandler @Inject constructor() {
    private var nfcAdapter: NfcAdapter? = null

    fun initialize(activity: Activity) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
    }

    fun isNfcAvailable(): Boolean {
        return nfcAdapter?.isEnabled == true
    }

    fun enableNfcReading(activity: Activity) {
        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        nfcAdapter?.enableForegroundDispatch(
            activity,
            null,
            null,
            null
        )
    }

    fun disableNfcReading(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun handleNfcIntent(intent: Intent): Tag? {
        return if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action
        ) {
            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        } else {
            null
        }
    }

    fun getIsoDep(tag: Tag): IsoDep? {
        return IsoDep.get(tag)
    }
} 