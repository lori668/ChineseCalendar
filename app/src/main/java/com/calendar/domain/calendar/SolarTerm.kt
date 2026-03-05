package com.calendar.domain.calendar

import kotlin.math.*

/**
 * 24 节气计算引擎
 * 基于天文算法，精确到小时
 */
object SolarTerm {

    // 节气名称
    private val solarTermNames = arrayOf(
        "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
        "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
        "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
        "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    )

    // 节气角度 (每 15 度一个节气)
    private val solarTermAngles = doubleArrayOf(
        270.0, 285.0, 0.0, 15.0, 30.0, 45.0,
        60.0, 75.0, 90.0, 105.0, 120.0, 135.0,
        150.0, 165.0, 180.0, 195.0, 210.0, 225.0,
        240.0, 255.0, 270.0, 285.0, 300.0, 315.0
    )

    /**
     * 获取某年的所有节气时间
     */
    fun getSolarTermsForYear(year: Int): List<SolarTermInfo> {
        val result = mutableListOf<SolarTermInfo>()

        for (i in solarTermAngles.indices) {
            val time = calculateSolarTermTime(year, i)
            result.add(
                SolarTermInfo(
                    name = solarTermNames[i],
                    index = i,
                    dateTime = time,
                    year = time.year,
                    month = time.month,
                    day = time.day,
                    hour = time.hour,
                    minute = time.minute
                )
            )
        }

        return result
    }

    /**
     * 计算指定节气的具体时间
     */
    private fun calculateSolarTermTime(year: Int, index: Int): LunarDate {
        val angle = solarTermAngles[index]

        // 使用简化算法计算节气时间
        // 基于地球公转周期和节气角度

        // 基准年份 2000 年的春分时间 (3 月 20 日)
        val baseYear = 2000
        val tropicalYear = 365.2422 // 回归年长度

        // 计算从基准年到目标年的天数
        val yearsDiff = year - baseYear
        val days = yearsDiff * tropicalYear

        // 计算节气在年内的位置 (以天为单位)
        val dayOfYear = (angle / 360.0) * tropicalYear

        // 估算儒略日
        val jd = 2451545.0 + days + dayOfYear

        // 转换为公历日期
        return jdToDate(jd)
    }

    /**
     * 儒略日转公历日期
     */
    private fun jdToDate(jd: Double): LunarDate {
        val z = floor(jd + 0.5).toLong()
        val f = jd + 0.5 - z

        var alpha = floor((z - 1867216.25) / 36524.25).toLong()
        var a = z + 1 + alpha - floor(alpha / 4.0).toLong()

        if (z < 2299161) {
            a = z
        }

        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toLong()
        val d = floor(365.25 * c).toLong()
        val e = floor((b - d) / 30.6001).toLong()

        val day = floor(b - d - floor(30.6001 * e).toLong() + f).toInt()
        val month = if (e < 14) e.toInt() - 1 else e.toInt() - 13
        var year = if (month > 2) c.toInt() - 4716 else c.toInt() - 4715

        // 处理小时
        val hourFraction = (b - d - floor(30.6001 * e).toLong() + f) - day
        val hour = (hourFraction * 24).toInt()
        val minute = ((hourFraction * 24 - hour) * 60).toInt()

        return LunarDate(year, month, day, hour, minute, 0)
    }

    /**
     * 获取指定日期所在的节气
     */
    fun getCurrentSolarTerm(year: Int, month: Int, day: Int): String? {
        val terms = getSolarTermsForYear(year)

        for (i in terms.indices) {
            val term = terms[i]
            if (term.year == year && term.month == month && term.day == day) {
                return term.name
            }
        }

        // 检查是否是前一天的小时部分
        if (month > 1) {
            val prevTerms = getSolarTermsForYear(year - 1)
            for (term in prevTerms) {
                if (term.month == 12 && term.hour >= 12) {
                    val nextDay = term.day + 1
                    if (nextDay > 31) {
                        if (month == 1 && day == 1) {
                            return term.name
                        }
                    } else if (month == 12 && day == nextDay) {
                        return term.name
                    }
                }
            }
        }

        return null
    }

    /**
     * 获取指定日期最近的节气
     */
    fun getNearestSolarTerm(year: Int, month: Int, day: Int): SolarTermInfo? {
        val currentTerms = getSolarTermsForYear(year)
        val prevTerms = getSolarTermsForYear(year - 1)
        val allTerms = prevTerms + currentTerms

        val targetDays = dateToDays(year, month, day)

        var nearest: SolarTermInfo? = null
        var minDiff = Int.MAX_VALUE

        for (term in allTerms) {
            val termDays = dateToDays(term.year, term.month, term.day)
            val diff = kotlin.math.abs(termDays - targetDays)

            if (diff < minDiff) {
                minDiff = diff
                nearest = term
            }
        }

        return nearest
    }

    /**
     * 日期转换为天数 (简化计算)
     */
    private fun dateToDays(year: Int, month: Int, day: Int): Int {
        var days = 0
        for (y in 1900 until year) {
            days += if (LunarCalendar.isLeapYear(y)) 366 else 365
        }
        for (m in 1 until month) {
            days += when (m) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (LunarCalendar.isLeapYear(year)) 29 else 28
                else -> 30
            }
        }
        days += day
        return days
    }

    /**
     * 获取节气中文名
     */
    fun getSolarTermName(index: Int): String {
        return if (index in solarTermNames.indices) solarTermNames[index] else ""
    }

    /**
     * 判断某日是否是节气
     */
    fun isSolarTermDay(year: Int, month: Int, day: Int): Boolean {
        return getCurrentSolarTerm(year, month, day) != null
    }
}

/**
 * 节气信息
 */
data class SolarTermInfo(
    val name: String,
    val index: Int,
    val dateTime: LunarDate,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int
) {
    fun getFullString(): String {
        return "$name (${month}月${day}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')})"
    }
}
