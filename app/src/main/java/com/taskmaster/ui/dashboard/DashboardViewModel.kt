package com.taskmaster.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmaster.common.Resource
import com.taskmaster.common.UiEvent
import com.taskmaster.domain.model.Priority
import com.taskmaster.domain.model.Status
import com.taskmaster.domain.model.Task
import com.taskmaster.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val syncTasksUseCase: SyncTasksUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow("All")
    private val _sortOption = MutableStateFlow(SortOption.DUE_DATE_ASC)
    private val _isRefreshing = MutableStateFlow(false)

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Get user email
            val emailFlow = getSessionUseCase.userEmail.stateIn(viewModelScope)
            
            val filtersFlow = combine(
                _searchQuery,
                _statusFilter,
                _sortOption
            ) { search, filter, sort ->
                Triple(search, filter, sort)
            }

            combine(
                getTasksUseCase(),
                filtersFlow,
                _isRefreshing,
                emailFlow
            ) { resource, filters, refreshing, email ->
                val (search, filter, sort) = filters
                val userEmail = email ?: ""
                
                when (resource) {
                    is Resource.Loading<*> -> {
                        val cachedTasks = resource.data as? List<Task>
                        _uiState.update { 
                            it.copy(
                                isLoading = cachedTasks == null,
                                tasks = filterAndSort(cachedTasks ?: emptyList(), search, filter, sort),
                                isRefreshing = refreshing,
                                userEmail = userEmail
                            ) 
                        }
                    }
                    is Resource.Success<*> -> {
                        val rawTasks = resource.data as List<Task>
                        val filteredTasks = filterAndSort(rawTasks, search, filter, sort)
                        
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                tasks = filteredTasks,
                                totalTasksCount = rawTasks.size,
                                pendingTasksCount = rawTasks.count { t -> t.status == Status.PENDING },
                                completedTasksCount = rawTasks.count { t -> t.status == Status.COMPLETED },
                                searchQuery = search,
                                statusFilter = filter,
                                sortOption = sort,
                                isRefreshing = refreshing,
                                errorMessage = null,
                                userEmail = userEmail
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = refreshing,
                                errorMessage = resource.message,
                                userEmail = userEmail
                            )
                        }
                        _uiEvent.emit(UiEvent.ShowSnackbar(resource.message))
                    }
                }
            }.collect()
        }
    }

    private fun filterAndSort(
        tasks: List<Task>,
        search: String,
        filter: String,
        sort: SortOption
    ): List<Task> {
        var result = tasks

        // 1. Search filter
        if (search.isNotEmpty()) {
            result = result.filter { it.title.contains(search, ignoreCase = true) }
        }

        // 2. Status filter
        if (filter != "All") {
            val status = Status.fromString(filter)
            result = result.filter { it.status == status }
        }

        // 3. Sorting
        result = when (sort) {
            SortOption.DUE_DATE_ASC -> result.sortedBy { it.dueDate }
            SortOption.DUE_DATE_DESC -> result.sortedByDescending { it.dueDate }
            SortOption.PRIORITY -> result.sortedByDescending {
                when (it.priority) {
                    Priority.HIGH -> 3
                    Priority.MEDIUM -> 2
                    Priority.LOW -> 1
                }
            }
        }

        return result
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: String) {
        _statusFilter.value = filter
    }

    fun onSortOptionChanged(option: SortOption) {
        _sortOption.value = option
    }

    fun refreshTasks() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncTasksUseCase().collect { resource ->
                if (resource !is Resource.Loading) {
                    _isRefreshing.value = false
                    if (resource is Resource.Error) {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Sync failed: ${resource.message}"))
                    } else {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Sync completed successfully"))
                    }
                }
            }
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            val updatedStatus = if (task.status == Status.COMPLETED) Status.PENDING else Status.COMPLETED
            val updatedTask = task.copy(status = updatedStatus)
            updateTaskUseCase(updatedTask).collect { resource ->
                if (resource is Resource.Error) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Failed to update task: ${resource.message}"))
                } else if (resource is Resource.Success) {
                    val message = if (updatedStatus == Status.COMPLETED) "Task completed" else "Task marked pending"
                    _uiEvent.emit(UiEvent.ShowSnackbar(message))
                }
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            deleteTaskUseCase(id).collect { resource ->
                if (resource is Resource.Error) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Failed to delete task: ${resource.message}"))
                } else if (resource is Resource.Success) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Task deleted successfully"))
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiEvent.emit(UiEvent.Navigate("login"))
        }
    }
}
