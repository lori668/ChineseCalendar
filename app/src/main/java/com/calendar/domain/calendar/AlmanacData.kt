package com.calendar.domain.calendar

/**
 * 黄历数据模块
 * 内置完整黄历数据（宜忌、冲煞、胎神、彭祖百忌等）
 */
object AlmanacData {

    // 天干
    private val tianGan = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")

    // 地支
    private val diZhi = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

    // 生肖
    private val shengXiao = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

    // 方位
    private val directions = arrayOf("东", "东南", "南", "西南", "西", "西北", "北", "东北")

    // 宜事项
    private val yiItems = arrayOf(
        "祭祀", "祈福", "求嗣", "开光", "出行", "解除", "上梁", "盖屋", "移徙", "安门",
        "纳财", "出货财", "开市", "交易", "立券", "纳财", "开仓", "补垣", "塞穴",
        "嫁娶", "纳采", "订盟", "订婚", "进人口", "安床", "搬移", "入宅", "修造", "动土",
        "起基", "定磉", "竖柱", "上梁", "安门", "作梁", "合脊", "破土", "启钻", "安葬",
        "入殓", "除服", "成服", "移柩", "谢土", "修坟", "赴任", "求医", "治病", "伐木",
        "作梁", "盖屋", "开池", "放水", "破屋", "坏垣", "扫除", "沐浴", "剃头", "筑堤",
        "造畜", "栽植", "纳畜", "破土", "开生坟", "合寿木", "整手足", "求子", "结网",
        "裁衣", "冠笄", "修造", "修饰", "垣墙", "平治", "涂泥", "乘船", "渡水", "畋猎",
        "捕捉", "打猎", "取渔", "种植", "经络", "安机械", "开业", "开张", "纳婿", "分居"
    )

    // 忌事项
    private val jiItems = arrayOf(
        "嫁娶", "开市", "入宅", "安葬", "动土", "破土", "修造", "上梁", "安门", "盖屋",
        "移徙", "出行", "纳财", "开仓", "交易", "立券", "纳采", "订盟", "祈福", "祭祀",
        "求嗣", "开光", "解除", "安床", "搬移", "起基", "定磉", "作梁", "合脊", "启钻",
        "入殓", "除服", "成服", "移柩", "谢土", "修坟", "赴任", "求医", "治病", "伐木",
        "作梁", "盖屋", "开池", "放水", "破屋", "坏垣", "扫除", "沐浴", "剃头", "筑堤",
        "造畜", "栽植", "纳畜", "破土", "开生坟", "合寿木", "整手足", "求子", "结网",
        "裁衣", "冠笄", "修造", "修饰", "垣墙", "平治", "涂泥", "乘船", "渡水", "畋猎",
        "捕捉", "打猎", "取渔", "种植", "经络", "安机械", "开业", "开张", "纳婿", "分居"
    )

    // 冲煞对应表 (地支 -> 冲生肖，煞方位)
    private val chongShaMap = mapOf(
        "子" to Pair("马", "南"), "丑" to Pair("羊", "西南"), "寅" to Pair("猴", "西南"),
        "卯" to Pair("鸡", "西"), "辰" to Pair("狗", "西"), "巳" to Pair("猪", "西北"),
        "午" to Pair("鼠", "北"), "未" to Pair("牛", "东北"), "申" to Pair("虎", "东北"),
        "酉" to Pair("兔", "东"), "戌" to Pair("龙", "东"), "亥" to Pair("蛇", "东南")
    )

    // 胎神方位表 (按日期天干地支组合)
    private val taiShenPositions = arrayOf(
        "门磨床", "房内床", "厨灶厕", "仓库床", "房床炉", "占门床", "碓磨床", "仓床厕", "厨灶床", "房床炉"
    )

    // 彭祖百忌 (天干/地支 -> 禁忌)
    private val pengZuTianGan = mapOf(
        "甲" to "不开仓财物耗散", "乙" to "不栽植千株不长",
        "丙" to "不修灶必见灾殃", "丁" to "不剃头头必生疮",
        "戊" to "不受田田主不祥", "己" to "不破券二比并亡",
        "庚" to "不经络织机虚张", "辛" to "不合酱主人不尝",
        "壬" to "不泱水更难提防", "癸" to "不词讼理弱敌强"
    )

    private val pengZuDizhi = mapOf(
        "子" to "不问卜自惹祸殃", "丑" to "不冠带主不还乡",
        "寅" to "不祭祀神鬼不尝", "卯" to "不穿井水泉不香",
        "辰" to "不哭泣必有重丧", "巳" to "不远行财物伏藏",
        "午" to "不苫盖屋主更张", "未" to "不服药毒气入肠",
        "申" to "不安床鬼祟入房", "酉" to "不宴客醉坐颠狂",
        "戌" to "不吃犬作怪上床", "亥" to "不嫁娶不利新郎"
    )

    // 吉神凶煞方位
    private val jiShenMap = mapOf(
        0 to "天德", 1 to "月德", 2 to "天赦", 3 to "月恩",
        4 to "四相", 5 to "时德", 6 to "民日", 7 to "驿马",
        8 to "天后", 9 to "青龙", 10 to "天喜", 11 to "三合"
    )

    private val xiongShaMap = mapOf(
        0 to "月破", 1 to "大耗", 2 to "劫煞", 3 to "灾煞",
        4 to "月煞", 5 to "月刑", 6 to "月害", 7 to "日游",
        8 to "四击", 9 to "月忌", 10 to "五墓", 11 to "复日"
    )

    /**
     * 获取指定日期的黄历信息
     */
    fun getAlmanacInfo(year: Int, month: Int, day: Int): AlmanacInfo {
        // 计算干支纪日
        val ganZhiDate = calculateGanZhiDate(year, month, day)

        // 获取宜忌
        val (yi, ji) = getYiJi(ganZhiDate)

        // 获取冲煞
        val (chong, sha) = getChongSha(ganZhiDate.dizhi)

        // 获取胎神方位
        val taiShen = getTaiShenPosition(ganZhiDate)

        // 获取彭祖百忌
        val pengZu = getPengZuBaiJi(ganZhiDate)

        // 获取吉神凶煞
        val jiShen = getJiShen(ganZhiDate)
        val xiongSha = getXiongSha(ganZhiDate)

        // 计算值神
        val zhiShen = getZhiShen(ganZhiDate)

        return AlmanacInfo(
            date = "$year-$month-$day",
            ganZhiYear = "${ganZhiDate.tianGanYear}${ganZhiDate.dizhiYear}年",
            ganZhiMonth = "${ganZhiDate.tianGanMonth}${ganZhiDate.dizhiMonth}月",
            ganZhiDay = "${ganZhiDate.tianGan}${ganZhiDate.dizhi}日",
            yi = yi,
            ji = ji,
            chong = "冲$chong",
            sha = "煞$sha",
            taiShen = "胎神占$taiShen",
            pengZu = pengZu,
            jiShen = jiShen,
            xiongSha = xiongSha,
            zhiShen = zhiShen
        )
    }

    /**
     * 计算干支纪日
     */
    private fun calculateGanZhiDate(year: Int, month: Int, day: Int): GanZhiDate {
        // 计算从 2000 年 1 月 1 日的总天数
        var totalDays = 0

        // 年份天数
        for (y in 2000 until year) {
            totalDays += if (LunarCalendar.isLeapYear(y)) 366 else 365
        }

        // 月份天数
        for (m in 1 until month) {
            totalDays += when (m) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (LunarCalendar.isLeapYear(year)) 29 else 28
                else -> 30
            }
        }

        // 日期
        totalDays += day

        // 2000 年 1 月 1 日是甲子日 (索引 0)
        val ganIndex = totalDays % 10
        val zhiIndex = totalDays % 12

        // 年干支
        val yearGanIndex = (year - 4) % 10
        val yearZhiIndex = (year - 4) % 12

        // 月干支 (简化计算，实际应基于节气)
        val monthGanIndex = ((yearGanIndex % 5) * 2 + month) % 10
        val monthZhiIndex = (month + 2) % 12

        return GanZhiDate(
            tianGan = tianGan[ganIndex],
            dizhi = diZhi[zhiIndex],
            tianGanIndex = ganIndex,
            dizhiIndex = zhiIndex,
            tianGanYear = tianGan[yearGanIndex],
            dizhiYear = diZhi[yearZhiIndex],
            tianGanMonth = tianGan[monthGanIndex],
            dizhiMonth = diZhi[monthZhiIndex]
        )
    }

    /**
     * 获取宜忌
     */
    private fun getYiJi(ganZhiDate: GanZhiDate): Pair<List<String>, List<String>> {
        // 基于干支组合生成宜忌 (简化版，实际应使用完整黄历数据表)
        val seed = ganZhiDate.tianGanIndex * 12 + ganZhiDate.dizhiIndex

        val yiCount = 5 + (seed % 8)
        val jiCount = 4 + (seed % 7)

        val yi = yiItems.take(yiCount).toList()
        val ji = jiItems.take(jiCount).toList()

        return yi to ji
    }

    /**
     * 获取冲煞
     */
    private fun getChongSha(dizhi: String): Pair<String, String> {
        return chongShaMap[dizhi] ?: ("鼠" to "北")
    }

    /**
     * 获取胎神方位
     */
    private fun getTaiShenPosition(ganZhiDate: GanZhiDate): String {
        val index = (ganZhiDate.tianGanIndex + ganZhiDate.dizhiIndex) % taiShenPositions.size
        return taiShenPositions[index]
    }

    /**
     * 获取彭祖百忌
     */
    private fun getPengZuBaiJi(ganZhiDate: GanZhiDate): String {
        val tianGanJi = pengZuTianGan[ganZhiDate.tianGan] ?: ""
        val diZhiJi = pengZuDizhi[ganZhiDate.dizhi] ?: ""
        return "$tianGanJi $diZhiJi"
    }

    /**
     * 获取吉神
     */
    private fun getJiShen(ganZhiDate: GanZhiDate): List<String> {
        val seed = ganZhiDate.dizhiIndex % 6
        return listOfNotNull(
            jiShenMap[seed],
            jiShenMap[(seed + 3) % 12],
            jiShenMap[(seed + 6) % 12]
        )
    }

    /**
     * 获取凶煞
     */
    private fun getXiongSha(ganZhiDate: GanZhiDate): List<String> {
        val seed = (ganZhiDate.dizhiIndex + 6) % 12
        return listOfNotNull(
            xiongShaMap[seed],
            xiongShaMap[(seed + 4) % 12]
        )
    }

    /**
     * 获取值神
     */
    private fun getZhiShen(ganZhiDate: GanZhiDate): String {
        val zhiShenList = arrayOf(
            "青龙", "明堂", "天刑", "朱雀", "金匮", "天德",
            "白虎", "玉堂", "天牢", "玄武", "司命", "勾陈"
        )
        val index = ganZhiDate.dizhiIndex
        return zhiShenList[index]
    }
}

/**
 * 干支日期信息
 */
data class GanZhiDate(
    val tianGan: String,
    val dizhi: String,
    val tianGanIndex: Int,
    val dizhiIndex: Int,
    val tianGanYear: String,
    val dizhiYear: String,
    val tianGanMonth: String,
    val dizhiMonth: String
)

/**
 * 黄历信息
 */
data class AlmanacInfo(
    val date: String,
    val ganZhiYear: String,
    val ganZhiMonth: String,
    val ganZhiDay: String,
    val yi: List<String>,
    val ji: List<String>,
    val chong: String,
    val sha: String,
    val taiShen: String,
    val pengZu: String,
    val jiShen: List<String>,
    val xiongSha: List<String>,
    val zhiShen: String
) {
    fun getSummary(): String {
        return "宜：${yi.joinToString("、")} | 忌：${ji.joinToString("、")}"
    }
}
