package com.taskmaster.domain.usecase

import com.taskmaster.data.local.datastore.SessionManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedInFlow
    val userEmail: Flow<String?> = sessionManager.userEmailFlow
    val isBiometricEnabled: Flow<Boolean> = sessionManager.isBiometricEnabledFlow

    suspend fun setBiometricEnabled(enabled: Boolean) {
        sessionManager.setBiometricEnabled(enabled)
    }
}
