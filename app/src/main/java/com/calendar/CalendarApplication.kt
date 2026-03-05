package com.calendar

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.calendar.data.AppDatabase
import com.calendar.data.repository.BirthdayRepository
import com.calendar.data.repository.TaskRepository

/**
 * 应用入口
 */
class CalendarApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val birthdayRepository by lazy { BirthdayRepository(database.birthdayDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // 生日提醒渠道
            val birthdayChannel = NotificationChannel(
                CHANNEL_BIRTHDAY,
                "生日提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "朋友/家人生日提醒通知"
                enableLights(true)
                enableVibration(true)
            }

            // 任务提醒渠道
            val taskChannel = NotificationChannel(
                CHANNEL_TASK,
                "任务提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "个人任务到期提醒通知"
                enableLights(true)
                enableVibration(true)
            }

            // 日常提醒渠道
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY,
                "日常提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "日历事件、节气等日常提醒"
                enableLights(false)
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(
                listOf(birthdayChannel, taskChannel, dailyChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_BIRTHDAY = "birthday_reminder"
        const val CHANNEL_TASK = "task_reminder"
        const val CHANNEL_DAILY = "daily_reminder"
    }
}
