package com.taskmaster.domain.usecase

import com.taskmaster.common.Resource
import com.taskmaster.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> {
        return repository.syncPendingTasks()
    }
}
