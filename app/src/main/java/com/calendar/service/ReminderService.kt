package com.calendar.service

import android.content.Context
import com.calendar.data.AppDatabase

/**
 * 提醒服务
 * 负责管理和调度所有提醒
 */
object ReminderService {

    /**
     * 调度所有提醒
     * 调用此方法会重新注册数据库中所有启用的提醒
     */
    fun scheduleAllReminders(context: Context) {
        val database = AppDatabase.getDatabase(context)

        // 调度所有生日提醒
        val birthdays = database.birthdayDao().getAllBirthdaysSync()
        birthdays.forEach { birthday ->
            if (birthday.reminderEnabled) {
                AlarmScheduler.scheduleBirthdayReminder(context, birthday)
            }
        }

        // 调度所有任务提醒
        val tasks = database.taskDao().getAllTasksSync()
        tasks.forEach { task ->
            if (task.reminderEnabled && task.status != Task.TaskStatus.COMPLETED) {
                AlarmScheduler.scheduleTaskReminder(context, task)
            }
        }
    }

    /**
     * 重新调度生日提醒
     */
    fun rescheduleBirthdayReminder(context: Context, birthdayId: Long) {
        // 先取消旧提醒
        AlarmScheduler.cancelReminder(context, "birthday", birthdayId)

        // 重新注册
        val database = AppDatabase.getDatabase(context)
        val birthday = database.birthdayDao().getBirthdayById(birthdayId)
        birthday?.let {
            if (it.reminderEnabled) {
                AlarmScheduler.scheduleBirthdayReminder(context, it)
            }
        }
    }

    /**
     * 重新调度任务提醒
     */
    fun rescheduleTaskReminder(context: Context, taskId: Long) {
        // 先取消旧提醒
        AlarmScheduler.cancelReminder(context, "task", taskId)

        // 重新注册
        val database = AppDatabase.getDatabase(context)
        val task = database.taskDao().getTaskById(taskId)
        task?.let {
            if (it.reminderEnabled && it.status != Task.TaskStatus.COMPLETED) {
                AlarmScheduler.scheduleTaskReminder(context, it)
            }
        }
    }

    /**
     * 取消生日提醒
     */
    fun cancelBirthdayReminder(context: Context, birthdayId: Long) {
        AlarmScheduler.cancelReminder(context, "birthday", birthdayId)
    }

    /**
     * 取消任务提醒
     */
    fun cancelTaskReminder(context: Context, taskId: Long) {
        AlarmScheduler.cancelReminder(context, "task", taskId)
    }
}
