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
import com.beakash.bereminder.data.ReminderDatabase
import com.beakash.bereminder.data.ReminderRepository
import com.beakash.bereminder.model.Reminder
import com.beakash.bereminder.model.RepeatEndMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "Time to take medicine"
        val intervalHours = intent.getIntExtra("intervalHours", 1)
        val isEnabled = intent.getBooleanExtra("isEnabled", true)

        val repeatEndModeName = intent.getStringExtra("repeatEndMode") ?: RepeatEndMode.NEVER.name
        val repeatEndMode = RepeatEndMode.valueOf(repeatEndModeName)

        val maxOccurrences = if (intent.hasExtra("maxOccurrences")) {
            intent.getIntExtra("maxOccurrences", 0)
        } else {
            null
        }

        val untilDateTimeMillis = if (intent.hasExtra("untilDateTimeMillis")) {
            intent.getLongExtra("untilDateTimeMillis", 0L)
        } else {
            null
        }

        val currentOccurrences = intent.getIntExtra("currentOccurrences", 0)
        val newOccurrences = currentOccurrences + 1

        var updatedReminder = Reminder(
            id = intent.getIntExtra("id", 1),
            title = title,
            message = message,
            intervalHours = intervalHours,
            isEnabled = isEnabled,
            repeatEndMode = repeatEndMode,
            maxOccurrences = maxOccurrences,
            untilDateTimeMillis = untilDateTimeMillis,
            currentOccurrences = newOccurrences
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
            .setContentTitle(updatedReminder.title)
            .setContentText(updatedReminder.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(updatedReminder.id, notification)

        if (!updatedReminder.isEnabled) {
            persistReminder(context, updatedReminder)
            return
        }

        val nextTriggerAtMillis =
            System.currentTimeMillis() + updatedReminder.intervalHours * 60L * 1000L

        val shouldContinue = when (updatedReminder.repeatEndMode) {
            RepeatEndMode.NEVER -> true

            RepeatEndMode.AFTER_COUNT -> {
                val max = updatedReminder.maxOccurrences ?: 0
                updatedReminder.currentOccurrences < max
            }

            RepeatEndMode.UNTIL_DATE -> {
                val until = updatedReminder.untilDateTimeMillis ?: 0L
                nextTriggerAtMillis <= until
            }
        }

        if (!shouldContinue) {
            updatedReminder = updatedReminder.copy(isEnabled = false)
            persistReminder(context, updatedReminder)
            return
        }

        persistReminder(context, updatedReminder)

        AlarmScheduler(context).scheduleReminderAtTime(
            reminder = updatedReminder,
            triggerAtMillis = nextTriggerAtMillis
        )
    }

    private fun persistReminder(context: Context, reminder: Reminder) {
        val database = ReminderDatabase.getDatabase(context)
        val repository = ReminderRepository(database.reminderDao())

        CoroutineScope(Dispatchers.IO).launch {
            repository.updateReminder(reminder)
        }
    }
}