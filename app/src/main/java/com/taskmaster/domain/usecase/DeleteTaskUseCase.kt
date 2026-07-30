package com.taskmaster.domain.usecase

import com.taskmaster.common.Resource
import com.taskmaster.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(id: Long): Flow<Resource<Unit>> {
        return repository.deleteTask(id)
    }
}
