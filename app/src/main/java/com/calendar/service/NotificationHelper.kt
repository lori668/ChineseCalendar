package com.calendar.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.calendar.CalendarApplication
import com.calendar.ui.MainActivity

/**
 * 通知帮助类
 */
object NotificationHelper {

    /**
     * 显示生日提醒通知
     */
    fun showBirthdayReminder(context: Context, id: Long, title: String, message: String) {
        val notification = createNotification(
            context = context,
            channelId = CalendarApplication.CHANNEL_BIRTHDAY,
            title = title,
            message = message,
            icon = android.R.drawable.ic_dialog_alert,
            priority = NotificationCompat.PRIORITY_HIGH,
            category = NotificationCompat.CATEGORY_REMINDER
        )

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(id.toInt(), notification)
    }

    /**
     * 显示任务提醒通知
     */
    fun showTaskReminder(context: Context, id: Long, title: String, message: String) {
        val notification = createNotification(
            context = context,
            channelId = CalendarApplication.CHANNEL_TASK,
            title = title,
            message = message,
            icon = android.R.drawable.ic_dialog_alert,
            priority = NotificationCompat.PRIORITY_HIGH,
            category = NotificationCompat.CATEGORY_REMINDER
        )

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(id.toInt(), notification)
    }

    /**
     * 显示日常提醒通知
     */
    fun showDailyReminder(context: Context, id: Long, title: String, message: String) {
        val notification = createNotification(
            context = context,
            channelId = CalendarApplication.CHANNEL_DAILY,
            title = title,
            message = message,
            icon = android.R.drawable.ic_dialog_info,
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_STATUS
        )

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(id.toInt(), notification)
    }

    /**
     * 创建通知
     */
    private fun createNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        icon: Int,
        priority: Int,
        category: String
    ): Notification {
        // 点击通知打开应用
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setCategory(category)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(Notification.DEFAULT_ALL)
            .build()
    }
}
