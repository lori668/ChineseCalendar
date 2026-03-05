package com.calendar.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 任务实体
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,                     // 任务标题
    val description: String = "",          // 任务描述
    val category: String = "其他",          // 分类 (工作/生活/学习等)
    val priority: Priority = Priority.MEDIUM, // 优先级

    // 时间设置
    val deadlineYear: Int,                 // 截止年份
    val deadlineMonth: Int,                // 截止月份
    val deadlineDay: Int,                  // 截止日
    val deadlineHour: Int = 23,            // 截止小时
    val deadlineMinute: Int = 59,          // 截止分钟

    // 重复规则
    val repeatType: RepeatType = RepeatType.NONE, // 重复类型
    val repeatEndDate: Long? = null,       // 重复结束日期 (时间戳)

    // 提醒设置
    val reminderEnabled: Boolean = true,   // 是否启用提醒
    val reminderMinutesBefore: Int = 60,   // 提前多少分钟提醒

    // 状态
    val status: TaskStatus = TaskStatus.PENDING, // 任务状态
    val completedAt: Long? = null,         // 完成时间

    // 元数据
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    enum class Priority {
        LOW,      // 低
        MEDIUM,   // 中
        HIGH      // 高
    }

    enum class RepeatType {
        NONE,     // 不重复
        DAILY,    // 每日
        WEEKLY,   // 每周
        MONTHLY,  // 每月
        YEARLY    // 每年
    }

    enum class TaskStatus {
        PENDING,    // 待完成
        COMPLETED,  // 已完成
        OVERDUE     // 逾期
    }

    /**
     * 获取截止时间戳
     */
    fun getDeadlineTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(deadlineYear, deadlineMonth - 1, deadlineDay, deadlineHour, deadlineMinute)
        return calendar.timeInMillis
    }

    /**
     * 获取重复类型描述
     */
    fun getRepeatTypeDesc(): String {
        return when (repeatType) {
            RepeatType.NONE -> "不重复"
            RepeatType.DAILY -> "每日"
            RepeatType.WEEKLY -> "每周"
            RepeatType.MONTHLY -> "每月"
            RepeatType.YEARLY -> "每年"
        }
    }

    /**
     * 获取优先级描述
     */
    fun getPriorityDesc(): String {
        return when (priority) {
            Priority.LOW -> "低"
            Priority.MEDIUM -> "中"
            Priority.HIGH -> "高"
        }
    }

    /**
     * 获取状态描述
     */
    fun getStatusDesc(): String {
        return when (status) {
            TaskStatus.PENDING -> "待完成"
            TaskStatus.COMPLETED -> "已完成"
            TaskStatus.OVERDUE -> "逾期"
        }
    }
}
