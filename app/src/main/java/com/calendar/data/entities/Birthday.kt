package com.calendar.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 生日实体
 */
@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,                    // 姓名
    val relation: String,                // 关系 (父母/朋友/同事等)
    val birthdayType: String,            // 类型：solar(公历) / lunar(农历)
    val birthYear: Int,                  // 出生年份
    val birthMonth: Int,                 // 出生月份
    val birthDay: Int,                   // 出生日期
    val birthHour: Int = 0,              // 出生时辰 (可选)
    val remark: String = "",             // 备注 (喜欢的礼物、联系方式等)

    // 提醒设置
    val reminderEnabled: Boolean = true, // 是否启用提醒
    val reminderDaysBefore: Int = 1,     // 提前几天提醒 (1/3/7)
    val reminderTime: String = "09:00",  // 提醒时间 (HH:mm)

    // 元数据
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    /**
     * 获取完整生日描述
     */
    fun getBirthdayDesc(): String {
        val typeStr = if (birthdayType == "lunar") "农历" else "公历"
        return "$typeStr$birthYear 年$birthMonth 月$birthDay 日"
    }

    /**
     * 获取年龄
     */
    fun getAge(currentYear: Int): Int {
        return currentYear - birthYear
    }

    /**
     * 获取生肖
     */
    fun getShengXiao(): String {
        val shengXiaoArray = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
        return shengXiaoArray[(birthYear - 4) % 12]
    }
}
