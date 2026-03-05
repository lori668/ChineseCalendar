package com.calendar.domain.calendar

/**
 * 农历计算引擎
 * 基于天文算法和内置数据表，支持 1900-2100 年
 */
object LunarCalendar {

    // 农历数据表 (1900-2100 年)
    // 每 4 字节表示一年：
    // bit 0-3: 闰月月份 (0 表示无闰月)
    // bit 4-15: 13 个月的大小月情况 (1 为大月 30 天，0 为小月 29 天)
    // bit 16-19: 保留
    // bit 20-23: 春节的公历日期偏移
    private val lunarData = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x16a95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,
        0x0d520
    )

    // 天干
    private val tianGan = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")

    // 地支
    private val diZhi = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

    // 生肖
    private val shengXiao = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

    // 农历月份名称
    private val lunarMonths = arrayOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )

    // 日期名称
    private val lunarDays = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    /**
     * 获取农历年份信息
     */
    fun getLunarYearInfo(year: Int): LunarYearInfo {
        val offset = year - 1900
        if (offset < 0 || offset >= lunarData.size) {
            throw IllegalArgumentException("年份超出范围 (1900-2100)")
        }

        val data = lunarData[offset]
        val leapMonth = data and 0xF
        val hasLeap = leapMonth > 0

        // 计算每个月的天数
        val monthDays = IntArray(if (hasLeap) 13 else 12) { 29 }

        // 从高位到低位读取大小月信息
        var temp = data shr 4
        for (i in 11 downTo 0) {
            monthDays[i] += (temp and 1)
            temp = temp shr 1
        }

        // 如果有闰月，添加闰月天数
        if (hasLeap) {
            monthDays[leapMonth] += ((data shr 17) and 1)
        }

        // 计算春节日期
        val springFestivalDate = getSpringFestivalDate(year)

        return LunarYearInfo(
            year = year,
            leapMonth = leapMonth,
            hasLeap = hasLeap,
            monthDays = monthDays,
            springFestivalDate = springFestivalDate
        )
    }

    /**
     * 获取春节的公历日期
     */
    private fun getSpringFestivalDate(year: Int): LunarDate {
        val offset = year - 1900
        val data = lunarData[offset]
        val springOffset = (data shr 20) and 0xF

        // 基准日期：1 月 31 日 + 偏移
        var month = 1
        var day = 31 + springOffset

        val daysInMonth = getDaysInMonth(year, month)
        if (day > daysInMonth) {
            day -= daysInMonth
            month++
        }

        return LunarDate(year, month, day, 0, 0, 0)
    }

    /**
     * 获取某月天数
     */
    private fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    /**
     * 判断是否为公历闰年
     */
    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * 公历转农历
     */
    fun solarToLunar(year: Int, month: Int, day: Int): LunarDateInfo {
        // 计算到春节的总天数
        var days = day
        for (m in 1 until month) {
            days += getDaysInMonth(year, m)
        }

        // 计算从 1900 年到此年的总天数
        var totalDays = 0
        for (y in 1900 until year) {
            totalDays += if (isLeapYear(y)) 366 else 365
        }
        totalDays += days

        // 计算 1900 年春节的偏移
        val baseDays = getDaysFrom1900ToSpringFestival(1900)

        // 农历日期偏移
        val lunarOffset = totalDays - baseDays

        // 计算农历年月日
        var lunarYear = 1900
        var remainingDays = lunarOffset

        while (remainingDays >= 0) {
            val yearInfo = getLunarYearInfo(lunarYear)
            val yearDays = yearInfo.monthDays.sum()

            if (remainingDays < yearDays) {
                break
            }
            remainingDays -= yearDays
            lunarYear++
        }

        // 计算农历月和日
        val yearInfo = getLunarYearInfo(lunarYear)
        var lunarMonth = 1
        var isLeapMonth = false

        for (i in yearInfo.monthDays.indices) {
            val monthDays = yearInfo.monthDays[i]
            if (remainingDays < monthDays) {
                break
            }
            remainingDays -= monthDays
            lunarMonth = i + 1

            // 检查是否为闰月
            if (yearInfo.hasLeap && i == yearInfo.leapMonth) {
                isLeapMonth = true
                lunarMonth = i  // 闰月跟随前一个月
            }
        }

        val lunarDay = remainingDays + 1

        // 计算干支纪年
        val ganIndex = (lunarYear - 4) % 10
        val zhiIndex = (lunarYear - 4) % 12

        return LunarDateInfo(
            lunarYear = lunarYear,
            lunarMonth = lunarMonth,
            lunarDay = lunarDay,
            isLeapMonth = isLeapMonth,
            ganZhiYear = "${tianGan[ganIndex]}${diZhi[zhiIndex]}年",
            shengXiao = shengXiao[zhiIndex],
            lunarMonthStr = if (isLeapMonth) "闰${lunarMonths[lunarMonth - 1]}" else lunarMonths[lunarMonth - 1],
            lunarDayStr = if (lunarDay <= 30) lunarDays[lunarDay - 1] else "初一"
        )
    }

    /**
     * 计算从 1900 年到春节的天数
     */
    private fun getDaysFrom1900ToSpringFestival(year: Int): Int {
        var days = 0
        for (y in 1900 until year) {
            days += if (isLeapYear(y)) 366 else 365
        }
        // 1900 年春节是 1 月 31 日
        days += 30  // 1900 年 1 月 31 日是一年中的第 31 天，从 0 开始计是 30
        return days
    }
}

/**
 * 农历年份信息
 */
data class LunarYearInfo(
    val year: Int,
    val leapMonth: Int,
    val hasLeap: Boolean,
    val monthDays: IntArray,
    val springFestivalDate: LunarDate
)

/**
 * 农历日期信息
 */
data class LunarDateInfo(
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val isLeapMonth: Boolean,
    val ganZhiYear: String,
    val shengXiao: String,
    val lunarMonthStr: String,
    val lunarDayStr: String
) {
    fun getFullString(): String {
        return "${ganZhiYear}${lunarMonthStr}${lunarDayStr} ($shengXiao年)"
    }
}

/**
 * 基础农历日期
 */
data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0
)
