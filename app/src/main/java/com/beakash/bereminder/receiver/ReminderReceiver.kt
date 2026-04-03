package com.beakash.bereminder.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.beakash.bereminder.R
import com.beakash.bereminder.alarm.AlarmScheduler
import com.beakash.bereminder.model.Reminder

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "Time to take medicine"
        val intervalHours = intent.getIntExtra("intervalHours", 1)
        val isEnabled = intent.getBooleanExtra("isEnabled", true)

        val reminder = Reminder(
            id = intent.getIntExtra("id", 1),
            title = title,
            message = message,
            intervalHours = intervalHours,
            isEnabled = isEnabled
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(reminder.title)
            .setContentText("${reminder.message} (Repeats every ${reminder.intervalHours} hour(s))")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(reminder.id, notification)

        if (reminder.isEnabled) {
            val nextTriggerAtMillis =
                System.currentTimeMillis() + reminder.intervalHours * 60L * 1000L
            // change back to * 60L * 60L * 1000L for real hourly behavior later

            AlarmScheduler(context).scheduleReminderAtTime(
                reminder = reminder,
                triggerAtMillis = nextTriggerAtMillis
            )
        }
    }
}