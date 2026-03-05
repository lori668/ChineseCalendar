package com.calendar.data.dao

import androidx.room.*
import com.calendar.data.entities.Birthday
import kotlinx.coroutines.flow.Flow

/**
 * 生日数据访问对象
 */
@Dao
interface BirthdayDao {

    @Query("SELECT * FROM birthdays WHERE isDeleted = 0 ORDER BY birthMonth, birthDay")
    fun getAllBirthdays(): Flow<List<Birthday>>

    @Query("SELECT * FROM birthdays WHERE isDeleted = 0 AND id = :id")
    suspend fun getBirthdayById(id: Long): Birthday?

    @Query("SELECT * FROM birthdays WHERE isDeleted = 0 AND name LIKE :keyword ORDER BY birthMonth, birthDay")
    fun searchBirthdays(keyword: String): Flow<List<Birthday>>

    @Query("SELECT * FROM birthdays WHERE isDeleted = 0 AND birthMonth = :month ORDER BY birthDay")
    fun getBirthdaysInMonth(month: Int): Flow<List<Birthday>>

    @Query("SELECT * FROM birthdays WHERE isDeleted = 0 ORDER BY birthMonth, birthDay LIMIT :limit")
    fun getUpcomingBirthdays(limit: Int): Flow<List<Birthday>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthday(birthday: Birthday): Long

    @Update
    suspend fun updateBirthday(birthday: Birthday)

    @Delete
    suspend fun deleteBirthday(birthday: Birthday)

    @Query("UPDATE birthdays SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteBirthday(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE birthdays SET reminderEnabled = :enabled WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, enabled: Boolean)

    @Query("SELECT * FROM birthdays WHERE isDeleted = 0")
    suspend fun getAllBirthdaysSync(): List<Birthday>

    @Query("DELETE FROM birthdays WHERE isDeleted = 1")
    suspend fun clearDeletedBirthdays()
}
