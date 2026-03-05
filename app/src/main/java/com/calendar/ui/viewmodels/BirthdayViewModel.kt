package com.calendar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calendar.data.entities.Birthday
import com.calendar.data.repository.BirthdayRepository
import com.calendar.domain.calendar.LunarCalendar
import com.calendar.domain.calendar.LunarDateInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 生日 ViewModel
 */
class BirthdayViewModel(private val repository: BirthdayRepository) : ViewModel() {

    val allBirthdays: StateFlow<List<Birthday>> = repository.allBirthdays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingBirthdays: StateFlow<List<Birthday>> = repository
        .getUpcomingBirthdays(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchedBirthdays: StateFlow<List<Birthday>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allBirthdays
            } else {
                repository.searchBirthdays(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun addBirthday(birthday: Birthday) {
        viewModelScope.launch {
            repository.insertBirthday(birthday)
        }
    }

    fun updateBirthday(birthday: Birthday) {
        viewModelScope.launch {
            repository.updateBirthday(birthday)
        }
    }

    fun deleteBirthday(id: Long) {
        viewModelScope.launch {
            repository.deleteBirthday(id)
        }
    }

    fun toggleReminder(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateReminderStatus(id, enabled)
        }
    }

    /**
     * 计算距离生日的天数
     */
    fun getDaysUntilBirthday(birthday: Birthday): Int {
        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        var targetMonth = birthday.birthMonth
        var targetDay = birthday.birthDay

        // 如果是农历，需要转换
        if (birthday.birthdayType == "lunar") {
            // 简化处理，实际应该使用农历转换
            targetMonth = birthday.birthMonth
            targetDay = birthday.birthDay
        }

        val targetYear = if (targetMonth > currentMonth ||
            (targetMonth == currentMonth && targetDay >= currentDay)
        ) {
            currentYear
        } else {
            currentYear + 1
        }

        val currentDays = currentYear * 365 + currentMonth * 30 + currentDay
        val targetDays = targetYear * 365 + targetMonth * 30 + targetDay

        return targetDays - currentDays
    }

    /**
     * 获取生日的农历信息
     */
    fun getLunarInfoForBirthday(birthday: Birthday): LunarDateInfo {
        return if (birthday.birthdayType == "solar") {
            LunarCalendar.solarToLunar(
                birthday.birthYear,
                birthday.birthMonth,
                birthday.birthDay
            )
        } else {
            // 农历生日，返回基本信息
            LunarDateInfo(
                lunarYear = birthday.birthYear,
                lunarMonth = birthday.birthMonth,
                lunarDay = birthday.birthDay,
                isLeapMonth = false,
                ganZhiYear = "${LunarCalendar.solarToLunar(birthday.birthYear, 1, 1).ganZhiYear}",
                shengXiao = birthday.getShengXiao(),
                lunarMonthStr = LunarCalendar.solarToLunar(birthday.birthYear, birthday.birthMonth, 1).lunarMonthStr,
                lunarDayStr = if (birthday.birthDay <= 30)
                    com.calendar.domain.calendar.LunarCalendar::class.java
                        .getDeclaredField("lunarDays")
                        .apply { isAccessible = true }
                        .get(null) as Array<String>
                    else "初一"
            )
        }
    }
}
