package com.taskmaster.data.repository

import com.taskmaster.common.Resource
import com.taskmaster.data.local.dao.TaskDao
import com.taskmaster.data.local.entity.TaskEntity
import com.taskmaster.data.remote.api.ApiService
import com.taskmaster.utils.NetworkHelper
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TaskRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var taskDao: TaskDao
    private lateinit var networkHelper: NetworkHelper
    private lateinit var repository: TaskRepositoryImpl

    private val cachedEntities = listOf(
        TaskEntity(id = 1L, remoteId = 10, title = "Task 1", description = "", dueDate = 1000L, priority = "LOW", status = "Pending")
    )

    @Before
    fun setUp() {
        apiService = mock()
        taskDao = mock {
            on { getAllTasksFlow() } doReturn flowOf(cachedEntities)
        }
        networkHelper = mock()
        repository = TaskRepositoryImpl(apiService, taskDao, networkHelper)
    }

    @Test
    fun `getTasks returns cached items first`() = runTest {
        whenever(networkHelper.isNetworkConnected()).doReturn(false)
        
        val result = repository.getTasks()
        
        var lastResource: Resource<List<com.taskmaster.domain.model.Task>>? = null
        result.collect {
            lastResource = it
        }

        assert(lastResource is Resource.Success)
        val data = (lastResource as Resource.Success).data
        assertEquals(1, data.size)
        assertEquals("Task 1", data[0].title)
    }
}
