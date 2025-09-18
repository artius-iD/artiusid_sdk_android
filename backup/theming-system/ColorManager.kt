/*
 * File: ColorManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Manages color schemes for the SDK
 */
object ColorManager {
    private var _currentSchemeType by mutableStateOf(ColorSchemeType.LIGHT)
    private val _currentSchemeState = mutableStateOf(_currentSchemeType)
    
    fun getCurrentSchemeType(): ColorSchemeType = _currentSchemeType
    
    fun getCurrentSchemeState(): State<ColorSchemeType> = _currentSchemeState
    
    fun setSchemeType(schemeType: ColorSchemeType) {
        _currentSchemeType = schemeType
        _currentSchemeState.value = schemeType
    }
    
    fun getAvailableSchemes(): List<ColorSchemeType> {
        return ColorSchemeType.values().toList()
    }
    
    fun setColorScheme(schemeType: ColorSchemeType) {
        setSchemeType(schemeType)
    }
}
