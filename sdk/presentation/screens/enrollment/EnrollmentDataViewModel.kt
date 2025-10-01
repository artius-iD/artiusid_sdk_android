/*
 * File: EnrollmentDataViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.domain.model.EnrollmentData
import com.artiusid.sdk.domain.repository.EnrollmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

sealed class EnrollmentDataUiState {
    object Initial : EnrollmentDataUiState()
    object Processing : EnrollmentDataUiState()
    object Success : EnrollmentDataUiState()
    data class Error(val message: String) : EnrollmentDataUiState()
}

class EnrollmentDataViewModel(
    private val enrollmentRepository: EnrollmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EnrollmentDataUiState>(EnrollmentDataUiState.Initial)
    val uiState: StateFlow<EnrollmentDataUiState> = _uiState.asStateFlow()

    private val _enrollmentData = MutableStateFlow(EnrollmentData())
    val enrollmentData: StateFlow<EnrollmentData> = _enrollmentData.asStateFlow()

    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors.asStateFlow()

    // Validation patterns
    private val emailPattern = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
        Pattern.CASE_INSENSITIVE
    )
    private val phonePattern = Pattern.compile("^\\+?[1-9]\\d{1,14}$")
    private val zipCodePattern = Pattern.compile("^\\d{5}(-\\d{4})?$")
    private val ssnPattern = Pattern.compile("^\\d{3}-?\\d{2}-?\\d{4}$")
    private val datePattern = Pattern.compile("^(0[1-9]|1[0-2])/(0[1-9]|[12]\\d|3[01])/\\d{4}$")

    fun updateFirstName(value: String) {
        _enrollmentData.value = _enrollmentData.value.copy(firstName = value)
        validateField("firstName", value) { !it.isBlank() }
    }

    fun updateLastName(value: String) {
        _enrollmentData.value = _enrollmentData.value.copy(lastName = value)
        validateField("lastName", value) { !it.isBlank() }
    }

    fun updateEmail(value: String) {
        val formattedEmail = value.trim().lowercase()
        _enrollmentData.value = _enrollmentData.value.copy(email = formattedEmail)
        validateField("email", formattedEmail) { emailPattern.matcher(it).matches() }
    }

    fun updatePhoneNumber(value: String) {
        val formattedPhone = formatPhoneNumber(value)
        _enrollmentData.value = _enrollmentData.value.copy(phoneNumber = formattedPhone)
        validateField("phoneNumber", formattedPhone) { phonePattern.matcher(it).matches() }
    }

    fun updateAddress(value: String) {
        _enrollmentData.value = _enrollmentData.value.copy(address = value)
        validateField("address", value) { !it.isBlank() }
    }

    fun updateCity(value: String) {
        _enrollmentData.value = _enrollmentData.value.copy(city = value)
        validateField("city", value) { !it.isBlank() }
    }

    fun updateState(value: String) {
        val formattedState = value.uppercase()
        _enrollmentData.value = _enrollmentData.value.copy(state = formattedState)
        validateField("state", formattedState) { !it.isBlank() }
    }

    fun updateZipCode(value: String) {
        val formattedZip = formatZipCode(value)
        _enrollmentData.value = _enrollmentData.value.copy(zipCode = formattedZip)
        validateField("zipCode", formattedZip) { zipCodePattern.matcher(it).matches() }
    }

    fun updateDateOfBirth(value: String) {
        val formattedDate = formatDate(value)
        _enrollmentData.value = _enrollmentData.value.copy(dateOfBirth = formattedDate)
        validateField("dateOfBirth", formattedDate) { datePattern.matcher(it).matches() }
    }

    fun updateSsn(value: String) {
        val formattedSsn = formatSsn(value)
        _enrollmentData.value = _enrollmentData.value.copy(ssn = formattedSsn)
        validateField("ssn", formattedSsn) { ssnPattern.matcher(it).matches() }
    }

    fun submitEnrollmentData(data: EnrollmentData) {
        if (!validateAllFields()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = EnrollmentDataUiState.Processing
            try {
                enrollmentRepository.submitEnrollmentData(data)
                _uiState.value = EnrollmentDataUiState.Success
            } catch (e: Exception) {
                _uiState.value = EnrollmentDataUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = EnrollmentDataUiState.Initial
        _validationErrors.value = emptyMap()
    }

    fun getCurrentData(): EnrollmentData = _enrollmentData.value

    private fun validateAllFields(): Boolean {
        val data = _enrollmentData.value
        val errors = mutableMapOf<String, String>()

        // Required fields
        if (data.firstName.isBlank()) errors["firstName"] = "First name is required"
        if (data.lastName.isBlank()) errors["lastName"] = "Last name is required"
        if (data.email.isBlank()) errors["email"] = "Email is required"
        if (data.phoneNumber.isBlank()) errors["phoneNumber"] = "Phone number is required"
        if (data.address.isBlank()) errors["address"] = "Address is required"
        if (data.city.isBlank()) errors["city"] = "City is required"
        if (data.state.isBlank()) errors["state"] = "State is required"
        if (data.zipCode.isBlank()) errors["zipCode"] = "ZIP code is required"
        if (data.dateOfBirth.isBlank()) errors["dateOfBirth"] = "Date of birth is required"
        if (data.ssn.isBlank()) errors["ssn"] = "SSN is required"

        // Format validation
        if (!emailPattern.matcher(data.email).matches()) {
            errors["email"] = "Invalid email format"
        }
        if (!phonePattern.matcher(data.phoneNumber).matches()) {
            errors["phoneNumber"] = "Invalid phone number format"
        }
        if (!zipCodePattern.matcher(data.zipCode).matches()) {
            errors["zipCode"] = "Invalid ZIP code format"
        }
        if (!datePattern.matcher(data.dateOfBirth).matches()) {
            errors["dateOfBirth"] = "Invalid date format (MM/DD/YYYY)"
        }
        if (!ssnPattern.matcher(data.ssn).matches()) {
            errors["ssn"] = "Invalid SSN format (XXX-XX-XXXX)"
        }

        // Age validation
        try {
            val dob = SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(data.dateOfBirth)
            val age = calculateAge(dob)
            if (age < 18) {
                errors["dateOfBirth"] = "Must be at least 18 years old"
            }
        } catch (e: Exception) {
            errors["dateOfBirth"] = "Invalid date format"
        }

        _validationErrors.value = errors
        return errors.isEmpty()
    }

    private fun validateField(field: String, value: String, validator: (String) -> Boolean) {
        val errors = _validationErrors.value.toMutableMap()
        if (!validator(value)) {
            errors[field] = "Invalid ${field.replace(Regex("([A-Z])"), " $1").lowercase()}"
        } else {
            errors.remove(field)
        }
        _validationErrors.value = errors
    }

    private fun formatPhoneNumber(value: String): String {
        val digits = value.filter { it.isDigit() }
        return when {
            digits.length <= 3 -> digits
            digits.length <= 6 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
            else -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6, minOf(10, digits.length))}"
        }
    }

    private fun formatZipCode(value: String): String {
        val digits = value.filter { it.isDigit() }
        return when {
            digits.length <= 5 -> digits
            else -> "${digits.substring(0, 5)}-${digits.substring(5, minOf(9, digits.length))}"
        }
    }

    private fun formatDate(value: String): String {
        val digits = value.filter { it.isDigit() }
        return when {
            digits.length <= 2 -> digits
            digits.length <= 4 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
            else -> "${digits.substring(0, 2)}/${digits.substring(2, 4)}/${digits.substring(4, minOf(8, digits.length))}"
        }
    }

    private fun formatSsn(value: String): String {
        val digits = value.filter { it.isDigit() }
        return when {
            digits.length <= 3 -> digits
            digits.length <= 5 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
            else -> "${digits.substring(0, 3)}-${digits.substring(3, 5)}-${digits.substring(5, minOf(9, digits.length))}"
        }
    }

    private fun calculateAge(birthDate: Date?): Int {
        if (birthDate == null) return 0
        val today = Calendar.getInstance()
        val birthCalendar = Calendar.getInstance().apply { time = birthDate }
        var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }
} 