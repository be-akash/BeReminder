package com.beakash.bereminder.data

import com.beakash.bereminder.model.Reminder

class ReminderRepository(
    private val reminderDao: ReminderDao
) {
    suspend fun getAllReminders(): List<Reminder> {
        return reminderDao.getAllReminders().map { it.toReminder() }
    }

    suspend fun insertReminder(reminder: Reminder) {
        reminderDao.insertReminder(reminder.toEntity())
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder.toEntity())
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder.toEntity())
    }
}