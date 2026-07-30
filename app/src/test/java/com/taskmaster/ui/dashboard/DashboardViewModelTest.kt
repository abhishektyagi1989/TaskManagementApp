package com.taskmaster.ui.dashboard

import com.taskmaster.common.Resource
import com.taskmaster.domain.model.Priority
import com.taskmaster.domain.model.Status
import com.taskmaster.domain.model.Task
import com.taskmaster.domain.usecase.*
import com.taskmaster.ui.login.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getTasksUseCase: GetTasksUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var syncTasksUseCase: SyncTasksUseCase
    private lateinit var getSessionUseCase: GetSessionUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    
    private lateinit var viewModel: DashboardViewModel

    private val mockTasks = listOf(
        Task(id = 1L, title = "Task 1", description = "", dueDate = 1000L, priority = Priority.LOW, status = Status.PENDING),
        Task(id = 2L, title = "Task 2", description = "", dueDate = 2000L, priority = Priority.HIGH, status = Status.COMPLETED)
    )

    private val tasksFlow = flowOf(Resource.Success(mockTasks))
    private val emailFlow = flowOf("admin@test.com")
    private val isLoggedInFlow = flowOf(true)
    private val isBiometricEnabledFlow = flowOf(false)

    @Before
    fun setUp() {
        getTasksUseCase = mock {
            on { invoke() } doReturn tasksFlow
        }
        updateTaskUseCase = mock()
        deleteTaskUseCase = mock()
        syncTasksUseCase = mock()
        getSessionUseCase = mock {
            on { userEmail } doReturn emailFlow
            on { isLoggedIn } doReturn isLoggedInFlow
            on { isBiometricEnabled } doReturn isBiometricEnabledFlow
        }
        logoutUseCase = mock()
    }

    @Test
    fun `loadDashboardData updates tasks and statistics counts`() = runTest {
        viewModel = DashboardViewModel(
            getTasksUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            syncTasksUseCase,
            getSessionUseCase,
            logoutUseCase
        )

        val state = viewModel.uiState.value
        assertEquals(2, state.tasks.size)
        assertEquals(2, state.totalTasksCount)
        assertEquals(1, state.pendingTasksCount)
        assertEquals(1, state.completedTasksCount)
    }

    @Test
    fun `onSearchQueryChanged filters tasks correctly`() = runTest {
        viewModel = DashboardViewModel(
            getTasksUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            syncTasksUseCase,
            getSessionUseCase,
            logoutUseCase
        )

        viewModel.onSearchQueryChanged("Task 2")
        val state = viewModel.uiState.value
        assertEquals(1, state.tasks.size)
        assertEquals("Task 2", state.tasks[0].title)
    }
}
