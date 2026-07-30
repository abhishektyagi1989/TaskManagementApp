package com.taskmaster.common

sealed interface UiEvent {
    data class Navigate(val route: String) : UiEvent
    data class ShowSnackbar(val message: String) : UiEvent
    object NavigateUp : UiEvent
}
