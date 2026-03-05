package com.calendar.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.calendar.data.AppDatabase
import com.calendar.data.entities.Birthday
import com.calendar.data.entities.Task
import com.calendar.receiver.AlarmReceiver
import java.util.Calendar

/**
 * 闹钟调度器
 * 负责 scheduled 所有提醒闹钟
 */
object AlarmScheduler {

    private const val REQUEST_CODE_BIRTHDAY_BASE = 10000
    private const val REQUEST_CODE_TASK_BASE = 20000

    /**
     * 注册生日提醒
     */
    fun scheduleBirthdayReminder(context: Context, birthday: Birthday) {
        if (!birthday.reminderEnabled) return

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, birthday.reminderTime.split(':')[0].toInt())
        calendar.set(Calendar.MINUTE, birthday.reminderTime.split(':')[1].toInt())
        calendar.set(Calendar.SECOND, 0)

        // 计算提醒日期（生日前几天）
        val birthdayCalendar = Calendar.getInstance()
        birthdayCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        birthdayCalendar.set(Calendar.MONTH, birthday.birthMonth - 1)
        birthdayCalendar.set(Calendar.DAY_OF_MONTH, birthday.birthDay)

        // 如果今年生日已过，设置为明年
        if (birthdayCalendar.before(Calendar.getInstance())) {
            birthdayCalendar.add(Calendar.YEAR, 1)
        }

        // 减去提醒天数
        birthdayCalendar.add(Calendar.DAY_OF_YEAR, -birthday.reminderDaysBefore)

        // 设置与生日相同的时分
        birthdayCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
        birthdayCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))

        scheduleAlarm(
            context = context,
            triggerTime = birthdayCalendar.timeInMillis,
            reminderType = "birthday",
            reminderId = birthday.id,
            title = "生日提醒",
            message = "${birthday.name}的生日还有${birthday.reminderDaysBefore}天！"
        )
    }

    /**
     * 注册任务提醒
     */
    fun scheduleTaskReminder(context: Context, task: Task) {
        if (!task.reminderEnabled) return

        val calendar = Calendar.getInstance()
        calendar.set(
            task.deadlineYear,
            task.deadlineMonth - 1,
            task.deadlineDay,
            task.deadlineHour,
            task.deadlineMinute
        )

        // 减去提前提醒时间
        calendar.add(Calendar.MINUTE, -task.reminderMinutesBefore)

        scheduleAlarm(
            context = context,
            triggerTime = calendar.timeInMillis,
            reminderType = "task",
            reminderId = task.id,
            title = "任务提醒",
            message = "任务「${task.title}」即将到期！"
        )
    }

    /**
     * 注册闹钟
     */
    private fun scheduleAlarm(
        context: Context,
        triggerTime: Long,
        reminderType: String,
        reminderId: Long,
        title: String,
        message: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("reminder_type", reminderType)
            putExtra("title", title)
            putExtra("message", message)
            putExtra("reminder_id", reminderId)
        }

        val requestCode = when (reminderType) {
            "birthday" -> REQUEST_CODE_BIRTHDAY_BASE + reminderId.toInt()
            "task" -> REQUEST_CODE_TASK_BASE + reminderId.toInt()
            else -> reminderId.toInt()
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 设置闹钟
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ 需要检查权限
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // 没有精确闹钟权限，使用普通闹钟
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * 取消提醒
     */
    fun cancelReminder(context: Context, reminderType: String, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)

        val requestCode = when (reminderType) {
            "birthday" -> REQUEST_CODE_BIRTHDAY_BASE + reminderId.toInt()
            "task" -> REQUEST_CODE_TASK_BASE + reminderId.toInt()
            else -> reminderId.toInt()
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    /**
     * 取消所有生日提醒
     */
    fun cancelAllBirthdayReminders(context: Context, birthdayIds: List<Long>) {
        birthdayIds.forEach { id ->
            cancelReminder(context, "birthday", id)
        }
    }

    /**
     * 取消所有任务提醒
     */
    fun cancelAllTaskReminders(context: Context, taskIds: List<Long>) {
        taskIds.forEach { id ->
            cancelReminder(context, "task", id)
        }
    }
}
