package com.beakash.bereminder.data

import com.beakash.bereminder.model.Reminder

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        title = title,
        message = message,
        intervalHours = intervalHours,
        isEnabled = isEnabled
    )
}

fun ReminderEntity.toReminder(): Reminder {
    return Reminder(
        id = id,
        title = title,
        message = message,
        intervalHours = intervalHours,
        isEnabled = isEnabled
    )
}