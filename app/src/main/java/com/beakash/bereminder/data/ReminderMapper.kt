package com.beakash.bereminder.data

import com.beakash.bereminder.model.Reminder
import com.beakash.bereminder.model.RepeatEndMode

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        title = title,
        message = message,
        intervalHours = intervalHours,
        isEnabled = isEnabled,
        repeatEndMode = repeatEndMode.name,
        maxOccurrences = maxOccurrences,
        untilDateTimeMillis = untilDateTimeMillis,
        currentOccurrences = currentOccurrences
    )
}

fun ReminderEntity.toReminder(): Reminder {
    return Reminder(
        id = id,
        title = title,
        message = message,
        intervalHours = intervalHours,
        isEnabled = isEnabled,
        repeatEndMode = RepeatEndMode.valueOf(repeatEndMode),
        maxOccurrences = maxOccurrences,
        untilDateTimeMillis = untilDateTimeMillis,
        currentOccurrences = currentOccurrences
    )
}