package com.taskmaster.domain.repository

import com.taskmaster.common.Resource
import com.taskmaster.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<Resource<List<Task>>>
    fun getTaskById(id: Long): Flow<Resource<Task?>>
    fun createTask(task: Task): Flow<Resource<Long>>
    fun updateTask(task: Task): Flow<Resource<Unit>>
    fun deleteTask(id: Long): Flow<Resource<Unit>>
    fun syncPendingTasks(): Flow<Resource<Unit>>
}
