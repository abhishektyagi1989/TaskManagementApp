package com.taskmaster.ui.dashboard

import com.taskmaster.domain.model.Task

data class DashboardUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val totalTasksCount: Int = 0,
    val pendingTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val searchQuery: String = "",
    val statusFilter: String = "All", // "All", "Pending", "Completed"
    val sortOption: SortOption = SortOption.DUE_DATE_ASC,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
    val userEmail: String = ""
)

enum class SortOption {
    DUE_DATE_ASC, DUE_DATE_DESC, PRIORITY
}
