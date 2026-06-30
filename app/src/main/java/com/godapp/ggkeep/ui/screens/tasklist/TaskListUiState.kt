package com.godapp.ggkeep.ui.screens.tasklist

import com.godapp.ggkeep.domain.model.KeepTask

data class TaskListUiState(
    val tasks: List<KeepTask> = emptyList(),
    val isLoading: Boolean = true
)
