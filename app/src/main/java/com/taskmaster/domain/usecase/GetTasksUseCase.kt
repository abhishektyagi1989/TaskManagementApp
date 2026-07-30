package com.taskmaster.domain.usecase

import com.taskmaster.common.Resource
import com.taskmaster.domain.model.Task
import com.taskmaster.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<Resource<List<Task>>> {
        return repository.getTasks()
    }
}
