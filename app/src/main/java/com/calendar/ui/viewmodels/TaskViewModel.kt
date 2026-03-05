package com.calendar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calendar.data.entities.Task
import com.calendar.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 任务 ViewModel
 */
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<Task>> = repository.pendingTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<Task>> = repository.completedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueTasks: StateFlow<List<Task>> = repository.overdueTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchedTasks: StateFlow<List<Task>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allTasks
            } else {
                repository.searchTasks(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val tasksByCategory: StateFlow<List<Task>> = _selectedCategory
        .flatMapLatest { category ->
            if (category == null) {
                repository.allTasks
            } else {
                repository.getTasksByCategory(category)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun completeTask(id: Long) {
        viewModelScope.launch {
            repository.updateTaskStatus(
                id,
                Task.TaskStatus.COMPLETED,
                System.currentTimeMillis()
            )
        }
    }

    fun uncompleteTask(id: Long) {
        viewModelScope.launch {
            repository.updateTaskStatus(id, Task.TaskStatus.PENDING)
        }
    }

    fun toggleReminder(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateReminderStatus(id, enabled)
        }
    }

    /**
     * 更新任务状态（检查是否逾期）
     */
    fun updateTaskStatuses() {
        viewModelScope.launch {
            val tasks = repository.getAllTasksSync()
            val now = System.currentTimeMillis()

            for (task in tasks) {
                if (task.status == Task.TaskStatus.PENDING) {
                    val deadline = task.getDeadlineTimestamp()
                    if (now > deadline) {
                        repository.updateTaskStatus(
                            task.id,
                            Task.TaskStatus.OVERDUE
                        )
                    }
                }
            }
        }
    }
}
