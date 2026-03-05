package com.calendar.data.dao

import androidx.room.*
import com.calendar.data.entities.Task
import kotlinx.coroutines.flow.Flow

/**
 * 任务数据访问对象
 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY deadlineYear, deadlineMonth, deadlineDay, deadlineHour, deadlineMinute")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND title LIKE :keyword ORDER BY deadlineYear, deadlineMonth, deadlineDay")
    fun searchTasks(keyword: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND status = 'PENDING' ORDER BY deadlineYear, deadlineMonth, deadlineDay")
    fun getPendingTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND status = 'OVERDUE' ORDER BY deadlineYear, deadlineMonth, deadlineDay")
    fun getOverdueTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND category = :category ORDER BY deadlineYear, deadlineMonth, deadlineDay")
    fun getTasksByCategory(category: String): Flow<List<Task>>

    @Query("SELECT DISTINCT category FROM tasks WHERE isDeleted = 0")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteTask(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: Task.TaskStatus, completedAt: Long? = null, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET reminderEnabled = :enabled WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, enabled: Boolean)

    @Query("SELECT * FROM tasks WHERE isDeleted = 0")
    suspend fun getAllTasksSync(): List<Task>

    @Query("DELETE FROM tasks WHERE isDeleted = 1")
    suspend fun clearDeletedTasks()
}
