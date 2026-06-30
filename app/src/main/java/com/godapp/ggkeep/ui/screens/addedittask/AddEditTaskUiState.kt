package com.godapp.ggkeep.ui.screens.addedittask

import java.time.LocalDate

data class AddEditTaskUiState(
    val name: String = "",
    val phoneNumber: String = "",
    val lastConsumeDate: LocalDate = LocalDate.now(),
    val reminderHour: Int = 9,
    val remindDaysBefore: Int = 7,
    val smsToNumber: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
) {
    val isFormValid: Boolean
        get() = name.isNotBlank() && phoneNumber.isNotBlank()
}
