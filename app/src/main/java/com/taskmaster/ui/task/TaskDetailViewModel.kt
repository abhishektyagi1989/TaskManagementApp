package com.taskmaster.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmaster.common.Resource
import com.taskmaster.common.UiEvent
import com.taskmaster.domain.model.Status
import com.taskmaster.domain.model.Task
import com.taskmaster.domain.usecase.DeleteTaskUseCase
import com.taskmaster.domain.usecase.GetTaskByIdUseCase
import com.taskmaster.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var currentTaskId: Long = -1L

    init {
        savedStateHandle.get<Long>("taskId")?.let { id ->
            if (id != -1L) {
                currentTaskId = id
                loadTask(id)
            }
        }
    }

    fun loadTask(id: Long) {
        viewModelScope.launch {
            getTaskByIdUseCase(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, task = resource.data, errorMessage = null) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                        _uiEvent.emit(UiEvent.ShowSnackbar(resource.message))
                    }
                }
            }
        }
    }

    fun toggleTaskComplete() {
        val currentTask = _uiState.value.task ?: return
        viewModelScope.launch {
            val updatedStatus = if (currentTask.status == Status.COMPLETED) Status.PENDING else Status.COMPLETED
            val updatedTask = currentTask.copy(status = updatedStatus)
            updateTaskUseCase(updatedTask).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(task = updatedTask) }
                    val message = if (updatedStatus == Status.COMPLETED) "Task completed" else "Task marked pending"
                    _uiEvent.emit(UiEvent.ShowSnackbar(message))
                } else if (resource is Resource.Error) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Failed to update status: ${resource.message}"))
                }
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            deleteTaskUseCase(currentTaskId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvent.emit(UiEvent.ShowSnackbar("Task deleted successfully"))
                        _uiEvent.emit(UiEvent.NavigateUp)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvent.emit(UiEvent.ShowSnackbar("Failed to delete task: ${resource.message}"))
                    }
                }
            }
        }
    }
}
