package com.beakash.bereminder.data

import com.beakash.bereminder.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepository(
    private val reminderDao: ReminderDao
) {
    val remindersFlow: Flow<List<Reminder>> =
        reminderDao.observeAllReminders().map { entities ->
            entities.map { it.toReminder() }
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