package com.taskmaster.data.repository

import com.taskmaster.common.Resource
import com.taskmaster.data.local.dao.TaskDao
import com.taskmaster.data.local.entity.TaskEntity
import com.taskmaster.data.remote.api.ApiService
import com.taskmaster.data.remote.dto.TaskDto
import com.taskmaster.domain.model.Priority
import com.taskmaster.domain.model.Status
import com.taskmaster.domain.model.SyncState
import com.taskmaster.domain.model.Task
import com.taskmaster.domain.repository.TaskRepository
import com.taskmaster.utils.NetworkHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val taskDao: TaskDao,
    private val networkHelper: NetworkHelper
) : TaskRepository {

    override fun getTasks(): Flow<Resource<List<Task>>> = flow {
        emit(Resource.Loading())

        // 1. Get cached tasks first
        val cachedEntities = taskDao.getAllTasksFlow().first()
        emit(Resource.Loading(cachedEntities.map { it.toDomain() }))

        // 2. Emit the final updated local cache
        taskDao.getAllTasksFlow().collect { updatedEntities ->
            emit(Resource.Success(updatedEntities.map { it.toDomain() }))
        }
    }

    override fun getTaskById(id: Long): Flow<Resource<Task?>> = flow {
        emit(Resource.Loading())
        val task = taskDao.getTaskById(id)?.toDomain()
        emit(Resource.Success(task))
    }

    override fun createTask(task: Task): Flow<Resource<Long>> = flow {
        emit(Resource.Loading())
        val initialEntity = task.toEntity().copy(syncState = "PENDING_INSERT")
        val localId = taskDao.insertTask(initialEntity)

        if (networkHelper.isNetworkConnected()) {
            try {
                val dto = TaskDto(title = task.title, completed = task.status == Status.COMPLETED)
                val response = apiService.createTask(dto)
                if (response.id != null) {
                    val syncedEntity = initialEntity.copy(
                        id = localId,
                        remoteId = response.id,
                        syncState = "SYNCED"
                    )
                    taskDao.insertTask(syncedEntity)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync created task, cached locally")
            }
        }
        emit(Resource.Success(localId))
    }

    override fun updateTask(task: Task): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        val original = taskDao.getTaskById(task.id)
        val targetSyncState = if (original?.syncState == "PENDING_INSERT") {
            "PENDING_INSERT"
        } else {
            "PENDING_UPDATE"
        }

        val updatedEntity = task.toEntity().copy(syncState = targetSyncState)
        taskDao.updateTask(updatedEntity)

        if (networkHelper.isNetworkConnected() && updatedEntity.syncState == "PENDING_UPDATE" && updatedEntity.remoteId != null) {
            try {
                val dto = TaskDto(
                    id = updatedEntity.remoteId,
                    title = updatedEntity.title,
                    completed = updatedEntity.status == "Completed"
                )
                apiService.updateTask(updatedEntity.remoteId, dto)
                taskDao.updateTask(updatedEntity.copy(syncState = "SYNCED"))
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync updated task, cached locally")
            }
        }
        emit(Resource.Success(Unit))
    }

    override fun deleteTask(id: Long): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val taskEntity = taskDao.getTaskById(id)
        if (taskEntity != null) {
            if (taskEntity.remoteId == null) {
                // Was never synced to server, delete locally immediately
                taskDao.deleteTaskById(id)
            } else {
                // Mark for deletion offline
                val markedEntity = taskEntity.copy(syncState = "PENDING_DELETE")
                taskDao.updateTask(markedEntity)

                if (networkHelper.isNetworkConnected()) {
                    try {
                        val response = apiService.deleteTask(taskEntity.remoteId)
                        if (response.isSuccessful) {
                            taskDao.deleteTaskById(id)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to sync delete task, marked for background sync")
                    }
                }
            }
        }
        emit(Resource.Success(Unit))
    }

    override fun syncPendingTasks(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        if (!networkHelper.isNetworkConnected()) {
            emit(Resource.Error("No internet connection available"))
            return@flow
        }

        val unsynced = taskDao.getUnsyncedTasks()
        var hasError = false

        unsynced.forEach { entity ->
            try {
                when (entity.syncState) {
                    "PENDING_INSERT" -> {
                        val dto = TaskDto(title = entity.title, completed = entity.status == "Completed")
                        val response = apiService.createTask(dto)
                        if (response.id != null) {
                            taskDao.insertTask(entity.copy(remoteId = response.id, syncState = "SYNCED"))
                        }
                    }
                    "PENDING_UPDATE" -> {
                        if (entity.remoteId != null) {
                            val dto = TaskDto(
                                id = entity.remoteId,
                                title = entity.title,
                                completed = entity.status == "Completed"
                            )
                            apiService.updateTask(entity.remoteId, dto)
                            taskDao.updateTask(entity.copy(syncState = "SYNCED"))
                        }
                    }
                    "PENDING_DELETE" -> {
                        if (entity.remoteId != null) {
                            val response = apiService.deleteTask(entity.remoteId)
                            if (response.isSuccessful) {
                                taskDao.deleteTaskById(entity.id)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error syncing task: ${entity.id}")
                hasError = true
            }
        }

        if (hasError) {
            emit(Resource.Error("Some tasks failed to sync"))
        } else {
            emit(Resource.Success(Unit))
        }
    }

    // Mapping extensions
    private fun TaskEntity.toDomain(): Task = Task(
        id = id,
        remoteId = remoteId,
        title = title,
        description = description,
        dueDate = dueDate,
        priority = Priority.fromString(priority),
        status = Status.fromString(status),
        syncState = SyncState.fromString(syncState)
    )

    private fun Task.toEntity(): TaskEntity = TaskEntity(
        id = id,
        remoteId = remoteId,
        title = title,
        description = description,
        dueDate = dueDate,
        priority = priority.name,
        status = status.name,
        syncState = syncState.name
    )
}
