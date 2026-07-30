package com.taskmaster.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmaster.common.UiEvent
import com.taskmaster.domain.usecase.GetSessionUseCase
import com.taskmaster.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getSessionUseCase: GetSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    init {
        viewModelScope.launch {
            // Monitor session status and biometric settings
            combine(
                getSessionUseCase.isLoggedIn,
                getSessionUseCase.userEmail,
                getSessionUseCase.isBiometricEnabled
            ) { isLoggedIn, email, isBiometric ->
                if (isLoggedIn) {
                    _uiEvent.emit(UiEvent.Navigate("dashboard"))
                }
                _uiState.update { 
                    it.copy(
                        isBiometricEnabled = isBiometric,
                        email = if (!email.isNullOrEmpty()) email else it.email
                    ) 
                }
            }.collect()
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = if (email.trim().isEmpty()) "Email cannot be empty" 
                             else if (!email.trim().matches(emailRegex)) "Invalid email format"
                             else null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = if (password.isEmpty()) "Password cannot be empty"
                                else if (password.length < 6) "Password must be at least 6 characters"
                                else null,
                errorMessage = null
            )
        }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        // Quick validate
        val emailError = if (email.isEmpty()) "Email cannot be empty" 
                         else if (!email.matches(emailRegex)) "Invalid email format"
                         else null
        
        val passwordError = if (password.isEmpty()) "Password cannot be empty"
                            else if (password.length < 6) "Password must be at least 6 characters"
                            else null

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = loginUseCase(email, password)
            _uiState.update { it.copy(isLoading = false) }
            result.onSuccess {
                _uiState.update { it.copy(loginSuccess = true) }
                _uiEvent.emit(UiEvent.Navigate("dashboard"))
            }.onFailure { exception ->
                _uiState.update { it.copy(errorMessage = exception.message) }
                _uiEvent.emit(UiEvent.ShowSnackbar(exception.message ?: "Authentication failed"))
            }
        }
    }

    fun loginBiometrically() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val savedEmail = getSessionUseCase.userEmail.first()
            val targetEmail = if (!savedEmail.isNullOrEmpty()) {
                savedEmail
            } else if (_uiState.value.email.trim().isNotEmpty()) {
                _uiState.value.email.trim()
            } else {
                "admin@test.com"
            }

            // Mock biometric login bypasses password verification
            loginUseCase(targetEmail, "123456").onSuccess {
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                _uiEvent.emit(UiEvent.Navigate("dashboard"))
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                _uiEvent.emit(UiEvent.ShowSnackbar(exception.message ?: "Biometric login failed"))
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            getSessionUseCase.setBiometricEnabled(enabled)
        }
    }
}
