package com.calendar.data.repository

import com.calendar.data.dao.TaskDao
import com.calendar.data.entities.Task
import kotlinx.coroutines.flow.Flow

/**
 * 任务数据仓库
 */
class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<Task>> = taskDao.getPendingTasks()
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasks()
    val overdueTasks: Flow<List<Task>> = taskDao.getOverdueTasks()

    fun getTasksByCategory(category: String): Flow<List<Task>> {
        return taskDao.getTasksByCategory(category)
    }

    fun searchTasks(keyword: String): Flow<List<Task>> {
        return taskDao.searchTasks("%$keyword%")
    }

    fun getAllCategories(): Flow<List<String>> {
        return taskDao.getAllCategories()
    }

    suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(id: Long) {
        taskDao.softDeleteTask(id)
    }

    suspend fun updateTaskStatus(id: Long, status: Task.TaskStatus, completedAt: Long? = null) {
        taskDao.updateTaskStatus(id, status, completedAt)
    }

    suspend fun updateReminderStatus(id: Long, enabled: Boolean) {
        taskDao.updateReminderStatus(id, enabled)
    }

    suspend fun getAllTasksSync(): List<Task> {
        return taskDao.getAllTasksSync()
    }

    suspend fun clearDeletedTasks() {
        taskDao.clearDeletedTasks()
    }
}
