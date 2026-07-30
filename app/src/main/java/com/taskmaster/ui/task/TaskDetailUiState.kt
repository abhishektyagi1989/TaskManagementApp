package com.taskmaster.ui.task

import com.taskmaster.domain.model.Task

data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val errorMessage: String? = null
)
