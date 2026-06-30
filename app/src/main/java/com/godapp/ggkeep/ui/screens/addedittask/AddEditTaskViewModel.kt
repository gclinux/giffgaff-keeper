package com.godapp.ggkeep.ui.screens.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godapp.ggkeep.data.repository.KeepTaskRepository
import com.godapp.ggkeep.domain.model.KeepTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val repository: KeepTaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: -1L

    private val _uiState = MutableStateFlow(
        AddEditTaskUiState(
            isEditing = taskId > 0,
            isLoading = taskId > 0
        )
    )
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    init {
        if (taskId > 0) loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId) ?: return@launch
            _uiState.update {
                it.copy(
                    name = task.name,
                    phoneNumber = task.phoneNumber,
                    lastConsumeDate = LocalDate.now(ZoneId.systemDefault())
                        .withEpochMilli(task.lastConsumeDate),
                    reminderHour = task.reminderHour,
                    remindDaysBefore = task.remindDaysBefore,
                    smsToNumber = task.smsToNumber.orEmpty(),
                    isLoading = false
                )
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updatePhoneNumber(value: String) = _uiState.update { it.copy(phoneNumber = value) }
    fun updateLastConsumeDate(value: LocalDate) = _uiState.update { it.copy(lastConsumeDate = value) }
    fun updateReminderHour(value: Int) = _uiState.update { it.copy(reminderHour = value) }
    fun updateRemindDaysBefore(value: Int) = _uiState.update { it.copy(remindDaysBefore = value) }
    fun updateSmsToNumber(value: String) = _uiState.update { it.copy(smsToNumber = value) }

    fun save() {
        val state = _uiState.value
        if (!state.isFormValid || state.isSaving) return
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val lastConsumeTs = state.lastConsumeDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val smsNumber = state.smsToNumber.trim().ifBlank { null }

                val task = KeepTask(
                    id = if (state.isEditing) taskId else 0L,
                    name = state.name.trim(),
                    phoneNumber = state.phoneNumber.trim(),
                    lastConsumeDate = lastConsumeTs,
                    reminderHour = state.reminderHour,
                    remindDaysBefore = state.remindDaysBefore,
                    smsToNumber = smsNumber
                )
                if (state.isEditing) {
                    repository.updateTask(task)
                } else {
                    repository.createTask(task)
                }
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "保存失败") }
            }
        }
    }

    private fun LocalDate.withEpochMilli(epochMilli: Long): LocalDate =
        java.time.Instant.ofEpochMilli(epochMilli)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
}
