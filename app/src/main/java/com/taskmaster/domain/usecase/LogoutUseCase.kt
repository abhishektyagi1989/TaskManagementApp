package com.taskmaster.domain.usecase

import com.taskmaster.data.local.datastore.SessionManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke() {
        sessionManager.clearSession()
    }
}
