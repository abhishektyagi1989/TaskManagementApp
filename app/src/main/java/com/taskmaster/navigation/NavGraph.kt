package com.taskmaster.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.taskmaster.ui.dashboard.DashboardScreen
import com.taskmaster.ui.login.LoginScreen
import com.taskmaster.ui.task.TaskDetailScreen
import com.taskmaster.ui.task.TaskFormScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToCreateTask = {
                    navController.navigate(Screen.TaskForm.passTaskId())
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Screen.TaskForm.passTaskId(taskId))
                },
                onNavigateToTaskDetails = { taskId ->
                    navController.navigate(Screen.TaskDetails.passTaskId(taskId))
                }
            )
        }

        composable(
            route = Screen.TaskForm.route,
            arguments = listOf(navArgument("taskId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) {
            TaskFormScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.TaskDetails.route,
            arguments = listOf(navArgument("taskId") {
                type = NavType.LongType
            }),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "taskmaster://task/detail/{taskId}"
                }
            )
        ) {
            TaskDetailScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Screen.TaskForm.passTaskId(taskId)) {
                        popUpTo(Screen.TaskDetails.route)
                    }
                }
            )
        }
    }
}
