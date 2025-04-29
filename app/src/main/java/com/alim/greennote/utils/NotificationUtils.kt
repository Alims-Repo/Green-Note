package com.alim.greennote.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alim.greennote.data.model.ModelTask

@SuppressLint("ScheduleExactAlarm")
fun Context.scheduleNotification(task: ModelTask) {
    val intent = Intent(this, ReminderReceiver::class.java).apply {
        putExtra("title", task.title)
        putExtra("message", task.description)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        this, task.id.toInt(), intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        task.dueDateMillis, pendingIntent
    )
}