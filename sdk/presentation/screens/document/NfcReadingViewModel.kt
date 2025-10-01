/*
 * File: NfcReadingViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.domain.model.PassportData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NfcReadingUiState {
    object Initial : NfcReadingUiState()
    object Reading : NfcReadingUiState()
    data class Success(val data: PassportData) : NfcReadingUiState()
    data class Error(val message: String) : NfcReadingUiState()
}

class NfcReadingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<NfcReadingUiState>(NfcReadingUiState.Initial)
    val uiState: StateFlow<NfcReadingUiState> = _uiState.asStateFlow()

    fun startNfcReading() {
        viewModelScope.launch {
            _uiState.value = NfcReadingUiState.Reading
        }
    }

    fun handleNfcTag(tag: Tag) {
        viewModelScope.launch {
            try {
                // TODO: Implement actual NFC tag reading
                // For now, just simulate a successful read
                kotlinx.coroutines.delay(2000)
                val passportData = PassportData(
                    passportNumber = "P12345678",
                    issuingCountry = "USA",
                    nationality = "USA",
                    dateOfBirth = "01/01/1990",
                    placeOfBirth = "New York",
                    sex = "M",
                    dateOfIssue = "01/01/2020",
                    dateOfExpiry = "01/01/2030",
                    issuingAuthority = "US Department of State",
                    personalNumber = "123456789",
                    surname = "Doe",
                    givenNames = "John"
                )
                
                // Store passport data for verification results
                com.artiusid.sdk.utils.DocumentDataHolder.setPassportData(
                    com.artiusid.sdk.utils.PassportData(
                        firstName = passportData.givenNames,
                        lastName = passportData.surname,
                        documentNumber = passportData.passportNumber,
                        nationality = passportData.nationality,
                        dateOfBirth = passportData.dateOfBirth,
                        dateOfExpiry = passportData.dateOfExpiry
                    )
                )
                android.util.Log.d("NfcReadingViewModel", "Stored passport data: firstName=${passportData.givenNames}, lastName=${passportData.surname}")
                
                _uiState.value = NfcReadingUiState.Success(passportData)
            } catch (e: Exception) {
                _uiState.value = NfcReadingUiState.Error(e.message ?: "NFC reading failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = NfcReadingUiState.Initial
    }
} 