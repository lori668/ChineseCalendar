package com.calendar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calendar.service.ReminderService

/**
 * 开机启动广播接收器
 * 手机开机后重新注册所有提醒
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            // 开机后重新注册所有提醒
            ReminderService.scheduleAllReminders(context)
        }
    }
}
