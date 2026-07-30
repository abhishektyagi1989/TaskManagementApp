package com.taskmaster.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object TaskForm : Screen("task/form?taskId={taskId}") {
        fun passTaskId(id: Long = -1L): String = "task/form?taskId=$id"
    }
    object TaskDetails : Screen("task/detail/{taskId}") {
        fun passTaskId(id: Long): String = "task/detail/$id"
    }
}
