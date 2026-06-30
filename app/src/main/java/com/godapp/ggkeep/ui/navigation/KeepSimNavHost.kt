package com.godapp.ggkeep.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.godapp.ggkeep.ui.screens.addedittask.AddEditTaskScreen
import com.godapp.ggkeep.ui.screens.commands.CommandsScreen
import com.godapp.ggkeep.ui.screens.taskdetail.TaskDetailScreen
import com.godapp.ggkeep.ui.screens.tasklist.TaskListScreen

object Routes {
    const val TASK_LIST = "tasklist"
    const val ADD_EDIT_TASK = "addedittask?taskId={taskId}"
    const val TASK_DETAIL = "taskdetail/{taskId}"
    const val COMMANDS = "commands"

    fun addEditTask(taskId: Long = -1L): String =
        if (taskId == -1L) "addedittask?taskId=-1" else "addedittask?taskId=$taskId"

    fun taskDetail(taskId: Long): String = "taskdetail/$taskId"
}

@Composable
fun KeepSimNavigation(
    initialTaskId: Long = -1L,
    triggerSms: Boolean = false
) {
    val navController = rememberNavController()

    // 始终以列表页为起始目的地，确保回退栈中有 tasklist
    NavHost(
        navController = navController,
        startDestination = Routes.TASK_LIST
    ) {
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                onAddTask = { navController.navigate(Routes.addEditTask()) },
                onTaskClick = { taskId -> navController.navigate(Routes.taskDetail(taskId)) }
            )
        }

        composable(
            route = Routes.ADD_EDIT_TASK,
            arguments = listOf(navArgument("taskId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
            AddEditTaskScreen(
                taskId = taskId,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TASK_DETAIL,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
            TaskDetailScreen(
                taskId = taskId,
                triggerSms = triggerSms,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.addEditTask(id)) },
                onDeleted = {
                    navController.popBackStack(Routes.TASK_LIST, inclusive = false)
                },
                onConsumed = { /* stay on detail; state refreshes */ },
                onOpenCommands = { navController.navigate(Routes.COMMANDS) }
            )
        }

        composable(Routes.COMMANDS) {
            CommandsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }

    // 如果是从 widget/通知启动，在列表页就绪后导航到详情页
    // 这样回退栈就是 tasklist → taskdetail/{taskId}，按返回键可回到列表
    LaunchedEffect(initialTaskId) {
        if (initialTaskId > 0) {
            navController.navigate(Routes.taskDetail(initialTaskId))
        }
    }
}
