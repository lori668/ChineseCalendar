package com.calendar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.CalendarApplication
import com.calendar.ui.viewmodels.BirthdayViewModel
import com.calendar.ui.viewmodels.BirthdayViewModelFactory
import com.calendar.ui.viewmodels.CalendarScreen
import com.calendar.ui.viewmodels.CalendarViewModel
import com.calendar.ui.viewmodels.SettingsScreen
import com.calendar.ui.viewmodels.TaskViewModel

/**
 * 主屏幕
 */
@Composable
fun MainScreen() {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as CalendarApplication

    val calendarViewModel: CalendarViewModel = viewModel()
    val birthdayViewModel: BirthdayViewModel = viewModel(
        factory = BirthdayViewModelFactory(application.birthdayRepository)
    )
    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(application.taskRepository)
    )

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val tabs = listOf(
                    Triple("日历", Icons.Default.CalendarToday, 0),
                    Triple("生日", Icons.Default.Cake, 1),
                    Triple("任务", Icons.Default.Checklist, 2),
                    Triple("设置", Icons.Default.Settings, 3)
                )

                tabs.forEach { (title, icon, index) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> CalendarScreen(viewModel = calendarViewModel)
                1 -> BirthdayScreen(viewModel = birthdayViewModel)
                2 -> TaskScreen(viewModel = taskViewModel)
                3 -> SettingsScreen()
            }
        }
    }
}

/**
 * BirthdayViewModel Factory
 */
class BirthdayViewModelFactory(private val repository: com.calendar.data.repository.BirthdayRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return com.calendar.ui.viewmodels.BirthdayViewModel(repository) as T
    }
}

/**
 * TaskViewModel Factory
 */
class TaskViewModelFactory(private val repository: com.calendar.data.repository.TaskRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return com.calendar.ui.viewmodels.TaskViewModel(repository) as T
    }
}
