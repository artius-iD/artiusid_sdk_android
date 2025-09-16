package com.artiusid.sdk.services

interface BarcodeScanManager {
    fun scanBarcode(imageData: ByteArray): String?
    fun isBarcodeDetected(): Boolean
} 