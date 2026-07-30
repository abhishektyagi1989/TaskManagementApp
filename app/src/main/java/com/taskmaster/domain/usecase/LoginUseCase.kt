package com.taskmaster.domain.usecase

import com.taskmaster.data.local.datastore.SessionManager
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        val emailClean = email.trim()
        if (emailClean.isEmpty()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        if (!emailClean.matches(emailRegex)) {
            return Result.failure(Exception("Invalid email format"))
        }
        if (password.isEmpty()) {
            return Result.failure(Exception("Password cannot be empty"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        // Mock Credentials
        return if (emailClean == "admin@test.com" && password == "123456") {
            sessionManager.saveSession(emailClean)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }
}
