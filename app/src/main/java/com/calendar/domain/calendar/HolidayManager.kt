package com.calendar.domain.calendar

/**
 * 法定节假日管理
 * 内置中国法定节假日和调休安排
 */
object HolidayManager {

    data class Holiday(
        val name: String,
        val date: String, // "MM-DD" 格式
        val type: HolidayType,
        val isWorkDay: Boolean = false, // 是否为调休补班
        val offDays: Int = 0 // 连休天数
    )

    enum class HolidayType {
        NEW_YEAR,       // 元旦
        SPRING,         // 春节
        QINGMING,       // 清明
        LABOUR,         // 劳动节
        DRAGON_BOAT,    // 端午
        MID_AUTUMN,     // 中秋
        NATIONAL,       // 国庆
        CUSTOM          // 自定义
    }

    // 固定日期节日
    private val fixedHolidays = mapOf(
        "01-01" to Holiday("元旦", "01-01", HolidayType.NEW_YEAR, offDays = 1),
        "05-01" to Holiday("劳动节", "05-01", HolidayType.LABOUR, offDays = 1),
        "10-01" to Holiday("国庆节", "10-01", HolidayType.NATIONAL, offDays = 7),
        "10-02" to Holiday("国庆节", "10-02", HolidayType.NATIONAL, offDays = 7),
        "10-03" to Holiday("国庆节", "10-03", HolidayType.NATIONAL, offDays = 7)
    )

    // 农历节日 (需要转换)
    private val lunarHolidays = mapOf(
        1 to 1 to "春节",
        1 to 15 to "元宵节",
        5 to 5 to "端午节",
        7 to 7 to "七夕节",
        8 to 15 to "中秋节",
        9 to 9 to "重阳节",
        12 to 8 to "腊八节",
        12 to 23 to "小年",
        12 to 30 to "除夕"
    )

    // 24 节气节日
    private val solarTermHolidays = setOf(
        "清明" to "清明节",
        "冬至" to "冬至"
    )

    /**
     * 获取指定日期的节日信息
     */
    fun getHoliday(year: Int, month: Int, day: Int): Holiday? {
        val dateKey = "${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

        // 检查固定日期节日
        fixedHolidays[dateKey]?.let {
            return it.copy(date = "$year-$dateKey")
        }

        // 检查特殊日期
        return getSpecialHoliday(year, month, day)
    }

    /**
     * 获取特殊日期 (基于农历)
     */
    private fun getSpecialHoliday(year: Int, month: Int, day: Int): Holiday? {
        val lunarInfo = LunarCalendar.solarToLunar(year, month, day)

        // 检查农历节日
        lunarHolidays[lunarInfo.lunarMonth to lunarInfo.lunarDay]?.let { name ->
            return Holiday(
                name = name,
                date = "$year-$month-$day",
                type = HolidayType.CUSTOM,
                offDays = 1
            )
        }

        // 检查节气节日
        val solarTerm = SolarTerm.getCurrentSolarTerm(year, month, day)
        solarTermHolidays.find { it.first == solarTerm }?.let {
            return Holiday(
                name = it.second,
                date = "$year-$month-$day",
                type = HolidayType.CUSTOM,
                offDays = 1
            )
        }

        return null
    }

    /**
     * 判断是否为法定节假日
     */
    fun isHoliday(year: Int, month: Int, day: Int): Boolean {
        return getHoliday(year, month, day) != null
    }

    /**
     * 判断是否为周末
     */
    fun isWeekend(year: Int, month: Int, day: Int): Boolean {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month - 1, day)
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        return dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY
    }

    /**
     * 判断是否为工作日 (包括调休)
     */
    fun isWorkDay(year: Int, month: Int, day: Int): Boolean {
        val holiday = getHoliday(year, month, day)
        if (holiday?.isWorkDay == true) return true

        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month - 1, day)
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)

        return dayOfWeek != java.util.Calendar.SATURDAY &&
               dayOfWeek != java.util.Calendar.SUNDAY &&
               holiday == null
    }

    /**
     * 获取月份节日列表
     */
    fun getHolidaysInMonth(year: Int, month: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()
        val daysInMonth = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (LunarCalendar.isLeapYear(year)) 29 else 28
            else -> 30
        }

        for (day in 1..daysInMonth) {
            getHoliday(year, month, day)?.let {
                holidays.add(it)
            }
        }

        return holidays
    }

    /**
     * 获取节日名称
     */
    fun getHolidayName(type: HolidayType): String {
        return when (type) {
            HolidayType.NEW_YEAR -> "元旦"
            HolidayType.SPRING -> "春节"
            HolidayType.QINGMING -> "清明"
            HolidayType.LABOUR -> "劳动节"
            HolidayType.DRAGON_BOAT -> "端午"
            HolidayType.MID_AUTUMN -> "中秋"
            HolidayType.NATIONAL -> "国庆"
            HolidayType.CUSTOM -> "节日"
        }
    }
}
