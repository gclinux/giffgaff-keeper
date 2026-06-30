package com.godapp.ggkeep.ui.screens.tasklist

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
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: KeepTaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAllTasks().collect { tasks ->
                val sorted = tasks.sortedWith(
                    compareBy<KeepTask> { it.status.ordinal }
                        .thenBy { it.remainingDays }
                )
                _uiState.update { it.copy(tasks = sorted, isLoading = false) }
            }
        }
    }

    fun confirmConsume(taskId: Long) {
        viewModelScope.launch {
            repository.confirmConsume(taskId)
        }
    }
}
