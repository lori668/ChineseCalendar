package com.calendar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calendar.domain.calendar.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日历 ViewModel
 */
class CalendarViewModel : ViewModel() {

    private val _currentDate = MutableStateFlow(Calendar.getInstance())
    val currentDate: StateFlow<Calendar> = _currentDate.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _viewType = MutableStateFlow(CalendarViewType.MONTH)
    val viewType: StateFlow<CalendarViewType> = _viewType.asStateFlow()

    private val _lunarDateInfo = MutableStateFlow<LunarDateInfo?>(null)
    val lunarDateInfo: StateFlow<LunarDateInfo?> = _lunarDateInfo.asStateFlow()

    private val _almanacInfo = MutableStateFlow<AlmanacInfo?>(null)
    val almanacInfo: StateFlow<AlmanacInfo?> = _almanacInfo.asStateFlow()

    private val _solarTerm = MutableStateFlow<String?>(null)
    val solarTerm: StateFlow<String?> = _solarTerm.asStateFlow()

    private val _holiday = MutableStateFlow<HolidayManager.Holiday?>(null)
    val holiday: StateFlow<HolidayManager.Holiday?> = _holiday.asStateFlow()

    val calendarDays: StateFlow<List<CalendarDay>> = _selectedDate
        .flatMapLatest { date ->
            flow {
                emit(generateCalendarDays(date))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    enum class CalendarViewType {
        YEAR, MONTH, WEEK, DAY
    }

    data class CalendarDay(
        val date: Calendar,
        val isCurrentMonth: Boolean,
        val isToday: Boolean,
        val isSelected: Boolean,
        val lunarInfo: LunarDateInfo?,
        val holiday: String?,
        val solarTerm: String?,
        val isWeekend: Boolean
    )

    init {
        updateDateInfo()
    }

    fun setViewType(type: CalendarViewType) {
        _viewType.value = type
    }

    fun navigateToPrevious() {
        val calendar = _currentDate.value.clone() as Calendar
        when (_viewType.value) {
            CalendarViewType.YEAR -> calendar.add(Calendar.YEAR, -1)
            CalendarViewType.MONTH -> calendar.add(Calendar.MONTH, -1)
            CalendarViewType.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            CalendarViewType.DAY -> calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        _currentDate.value = calendar
    }

    fun navigateToNext() {
        val calendar = _currentDate.value.clone() as Calendar
        when (_viewType.value) {
            CalendarViewType.YEAR -> calendar.add(Calendar.YEAR, 1)
            CalendarViewType.MONTH -> calendar.add(Calendar.MONTH, 1)
            CalendarViewType.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            CalendarViewType.DAY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        _currentDate.value = calendar
    }

    fun navigateToToday() {
        _currentDate.value = Calendar.getInstance()
        _selectedDate.value = Calendar.getInstance()
        updateDateInfo()
    }

    fun selectDate(date: Calendar) {
        _selectedDate.value = date.clone() as Calendar
        updateDateInfo()
    }

    private fun updateDateInfo() {
        val calendar = _selectedDate.value
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // 农历信息
        viewModelScope.launch {
            try {
                _lunarDateInfo.value = LunarCalendar.solarToLunar(year, month, day)
            } catch (e: Exception) {
                _lunarDateInfo.value = null
            }
        }

        // 黄历信息
        viewModelScope.launch {
            try {
                _almanacInfo.value = AlmanacData.getAlmanacInfo(year, month, day)
            } catch (e: Exception) {
                _almanacInfo.value = null
            }
        }

        // 节气
        viewModelScope.launch {
            _solarTerm.value = SolarTerm.getCurrentSolarTerm(year, month, day)
        }

        // 节假日
        viewModelScope.launch {
            _holiday.value = HolidayManager.getHoliday(year, month, day)
        }
    }

    private fun generateCalendarDays(calendar: Calendar): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(year, month, 1)
        firstDayOfMonth.set(Calendar.HOUR_OF_DAY, 0)
        firstDayOfMonth.set(Calendar.MINUTE, 0)
        firstDayOfMonth.set(Calendar.SECOND, 0)

        val lastDayOfMonth = calendar.clone() as Calendar
        lastDayOfMonth.set(year, month + 1, 0)

        val today = Calendar.getInstance()

        // 计算起始偏移（周日为 0）
        val startOffset = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1

        // 添加上月的日期
        val prevMonthDays = calendar.clone() as Calendar
        prevMonthDays.set(year, month - 1, 1)
        val daysInPrevMonth = prevMonthDays.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in startOffset - 1 downTo 0) {
            val day = daysInPrevMonth - i
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(year, month - 1, day)
            days.add(createCalendarDay(dayCalendar, false, today))
        }

        // 添加本月的日期
        for (day in 1..lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(year, month, day)
            days.add(createCalendarDay(dayCalendar, true, today))
        }

        // 添加下月的日期（补齐 42 天）
        val remainingDays = 42 - days.size
        for (i in 1..remainingDays) {
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(year, month + 1, i)
            days.add(createCalendarDay(dayCalendar, false, today))
        }

        return days
    }

    private fun createCalendarDay(calendar: Calendar, isCurrentMonth: Boolean, today: Calendar): CalendarDay {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

        val isSelected = calendar.get(Calendar.YEAR) == _selectedDate.value.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == _selectedDate.value.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == _selectedDate.value.get(Calendar.DAY_OF_MONTH)

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

        val holiday = HolidayManager.getHoliday(year, month, day)?.name
        val solarTerm = SolarTerm.getCurrentSolarTerm(year, month, day)

        val lunarInfo = try {
            LunarCalendar.solarToLunar(year, month, day)
        } catch (e: Exception) {
            null
        }

        return CalendarDay(
            date = calendar,
            isCurrentMonth = isCurrentMonth,
            isToday = isToday,
            isSelected = isSelected,
            lunarInfo = lunarInfo,
            holiday = holiday,
            solarTerm = solarTerm,
            isWeekend = isWeekend
        )
    }

    fun getFormattedDate(): String {
        val year = _currentDate.value.get(Calendar.YEAR)
        val month = _currentDate.value.get(Calendar.MONTH) + 1
        return "$year 年$month 月"
    }

    fun getSelectedDateFormatted(): String {
        val sdf = SimpleDateFormat("yyyy 年 MM 月 dd 日", Locale.CHINA)
        return sdf.format(_selectedDate.value.time)
    }
}
