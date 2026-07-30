package com.taskmaster.ui.login

import com.taskmaster.domain.usecase.GetSessionUseCase
import com.taskmaster.domain.usecase.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.Assert.assertEquals
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var getSessionUseCase: GetSessionUseCase
    private lateinit var viewModel: LoginViewModel

    private val isLoggedInFlow = MutableStateFlow(false)
    private val userEmailFlow = MutableStateFlow<String?>(null)
    private val isBiometricEnabledFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        loginUseCase = mock()
        getSessionUseCase = mock {
            on { isLoggedIn } doReturn isLoggedInFlow
            on { userEmail } doReturn userEmailFlow
            on { isBiometricEnabled } doReturn isBiometricEnabledFlow
        }
    }

    @Test
    fun `initial uiState is default`() {
        viewModel = LoginViewModel(loginUseCase, getSessionUseCase)
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(false, state.isLoading)
        assertEquals(false, state.loginSuccess)
    }

    @Test
    fun `onEmailChanged updates email and validates`() {
        viewModel = LoginViewModel(loginUseCase, getSessionUseCase)
        viewModel.onEmailChanged("test")
        assertEquals("test", viewModel.uiState.value.email)
        assertEquals("Invalid email format", viewModel.uiState.value.emailError)
    }

    @Test
    fun `login with valid credentials invokes usecase and succeeds`() = runTest {
        whenever(loginUseCase(any(), any())).doReturn(Result.success(Unit))
        
        viewModel = LoginViewModel(loginUseCase, getSessionUseCase)
        viewModel.onEmailChanged("admin@test.com")
        viewModel.onPasswordChanged("123456")
        
        viewModel.login()
        
        assertEquals(true, viewModel.uiState.value.loginSuccess)
    }
}
