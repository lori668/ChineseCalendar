package com.calendar.data.repository

import com.calendar.data.dao.BirthdayDao
import com.calendar.data.entities.Birthday
import kotlinx.coroutines.flow.Flow

/**
 * 生日数据仓库
 */
class BirthdayRepository(private val birthdayDao: BirthdayDao) {

    val allBirthdays: Flow<List<Birthday>> = birthdayDao.getAllBirthdays()

    fun getBirthdaysByMonth(month: Int): Flow<List<Birthday>> {
        return birthdayDao.getBirthdaysInMonth(month)
    }

    fun searchBirthdays(keyword: String): Flow<List<Birthday>> {
        return birthdayDao.searchBirthdays("%$keyword%")
    }

    fun getUpcomingBirthdays(limit: Int = 7): Flow<List<Birthday>> {
        return birthdayDao.getUpcomingBirthdays(limit)
    }

    suspend fun getBirthdayById(id: Long): Birthday? {
        return birthdayDao.getBirthdayById(id)
    }

    suspend fun insertBirthday(birthday: Birthday): Long {
        return birthdayDao.insertBirthday(birthday)
    }

    suspend fun updateBirthday(birthday: Birthday) {
        birthdayDao.updateBirthday(birthday.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteBirthday(id: Long) {
        birthdayDao.softDeleteBirthday(id)
    }

    suspend fun updateReminderStatus(id: Long, enabled: Boolean) {
        birthdayDao.updateReminderStatus(id, enabled)
    }

    suspend fun getAllBirthdaysSync(): List<Birthday> {
        return birthdayDao.getAllBirthdaysSync()
    }

    suspend fun clearDeletedBirthdays() {
        birthdayDao.clearDeletedBirthdays()
    }
}
