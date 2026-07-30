package com.taskmaster.ui.task

import com.taskmaster.domain.model.Priority
import com.taskmaster.domain.model.Status

data class TaskFormUiState(
    val title: String = "",
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val priority: Priority = Priority.MEDIUM,
    val status: Status = Status.PENDING,
    val titleError: String? = null,
    val isLoading: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false
)
