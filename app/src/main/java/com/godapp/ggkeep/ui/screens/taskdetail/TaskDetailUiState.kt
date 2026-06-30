package com.godapp.ggkeep.ui.screens.taskdetail

import com.godapp.ggkeep.domain.model.KeepTask

data class TaskDetailUiState(
    val task: KeepTask? = null,
    val isLoading: Boolean = true,
    val showSmsConfirm: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showDatePicker: Boolean = false,
    val deleted: Boolean = false
)
