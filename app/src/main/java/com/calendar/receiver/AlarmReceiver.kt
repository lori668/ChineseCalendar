package com.calendar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calendar.service.NotificationHelper

/**
 * 闹钟广播接收器
 * 接收定时提醒广播并显示通知
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("reminder_type") ?: return
        val title = intent.getStringExtra("title") ?: ""
        val message = intent.getStringExtra("message") ?: ""
        val id = intent.getLongExtra("reminder_id", 0)

        when (type) {
            "birthday" -> {
                NotificationHelper.showBirthdayReminder(context, id, title, message)
            }
            "task" -> {
                NotificationHelper.showTaskReminder(context, id, title, message)
            }
            "daily" -> {
                NotificationHelper.showDailyReminder(context, id, title, message)
            }
        }
    }
}
