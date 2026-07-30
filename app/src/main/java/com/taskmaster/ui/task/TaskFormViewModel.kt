package com.taskmaster.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmaster.common.Resource
import com.taskmaster.common.UiEvent
import com.taskmaster.domain.model.Priority
import com.taskmaster.domain.model.Status
import com.taskmaster.domain.model.Task
import com.taskmaster.domain.usecase.CreateTaskUseCase
import com.taskmaster.domain.usecase.GetTaskByIdUseCase
import com.taskmaster.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskFormViewModel @Inject constructor(
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var currentTaskId: Long? = null

    init {
        // Retrieve taskId from navigation arguments if available
        savedStateHandle.get<Long>("taskId")?.let { id ->
            if (id != -1L) {
                loadTask(id)
            }
        }
    }

    private fun loadTask(id: Long) {
        currentTaskId = id
        _uiState.update { it.copy(isEditMode = true, isLoading = true) }
        
        viewModelScope.launch {
            getTaskByIdUseCase(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val task = resource.data
                        if (task != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    title = task.title,
                                    description = task.description,
                                    dueDate = task.dueDate,
                                    priority = task.priority,
                                    status = task.status
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false, errorMessage = "Task not found") }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                        _uiEvent.emit(UiEvent.ShowSnackbar(resource.message))
                    }
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleError = if (title.trim().isEmpty()) "Title cannot be empty" else null
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onDueDateChanged(dueDate: Long) {
        _uiState.update { it.copy(dueDate = dueDate) }
    }

    fun onPriorityChanged(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onStatusChanged(status: Status) {
        _uiState.update { it.copy(status = status) }
    }

    fun saveTask() {
        val title = _uiState.value.title.trim()
        val description = _uiState.value.description
        val dueDate = _uiState.value.dueDate
        val priority = _uiState.value.priority
        val status = _uiState.value.status

        if (title.isEmpty()) {
            _uiState.update { it.copy(titleError = "Title cannot be empty") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            if (_uiState.value.isEditMode && currentTaskId != null) {
                // Update
                val updatedTask = Task(
                    id = currentTaskId!!,
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    status = status
                )
                updateTaskUseCase(updatedTask).collect { resource ->
                    handleSaveResult(resource)
                }
            } else {
                // Create
                val newTask = Task(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    status = status
                )
                createTaskUseCase(newTask).collect { resource ->
                    handleSaveResult(resource)
                }
            }
        }
    }

    private suspend fun <T> handleSaveResult(resource: Resource<T>) {
        when (resource) {
            is Resource.Loading -> {
                _uiState.update { it.copy(isLoading = true) }
            }
            is Resource.Success -> {
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
                _uiEvent.emit(UiEvent.ShowSnackbar("Task saved successfully"))
                _uiEvent.emit(UiEvent.NavigateUp)
            }
            is Resource.Error -> {
                _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                _uiEvent.emit(UiEvent.ShowSnackbar("Error: ${resource.message}"))
            }
        }
    }
}
