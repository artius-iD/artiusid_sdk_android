package com.artiusid.sdk.ui.screens.document

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// PassportData class defined locally since domain model doesn't exist
data class PassportData(
    val passportNumber: String = "",
    val issuingCountry: String = "",
    val nationality: String = "",
    val dateOfBirth: String = "",
    val surname: String = "",
    val givenNames: String = ""
)

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
                // Simulate passport data reading
                val passportData = mapOf(
                    "passportNumber" to "P12345678",
                    "issuingCountry" to "USA",
                    "nationality" to "USA",
                    "dateOfBirth" to "01/01/1990",
                    "surname" to "Doe",
                    "givenNames" to "John"
                )
                
                // Store passport data for verification results
                // Simulated data storage - in full implementation would use proper data classes
                android.util.Log.d("NfcReadingViewModel", "Passport data read: $passportData")
                android.util.Log.d("NfcReadingViewModel", "Stored passport data: firstName=${passportData["givenNames"]}, lastName=${passportData["surname"]}")
                
                val passportDataObj = PassportData(
                    passportNumber = passportData["passportNumber"] ?: "",
                    issuingCountry = passportData["issuingCountry"] ?: "",
                    nationality = passportData["nationality"] ?: "",
                    dateOfBirth = passportData["dateOfBirth"] ?: "",
                    surname = passportData["surname"] ?: "",
                    givenNames = passportData["givenNames"] ?: ""
                )
                _uiState.value = NfcReadingUiState.Success(passportDataObj)
            } catch (e: Exception) {
                _uiState.value = NfcReadingUiState.Error(e.message ?: "NFC reading failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = NfcReadingUiState.Initial
    }
} 