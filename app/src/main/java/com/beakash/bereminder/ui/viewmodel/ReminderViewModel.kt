package com.beakash.bereminder.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beakash.bereminder.alarm.AlarmScheduler
import com.beakash.bereminder.data.ReminderDao
import com.beakash.bereminder.data.toEntity
import com.beakash.bereminder.data.toReminder
import com.beakash.bereminder.model.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReminderViewModel(
    private val reminderDao: ReminderDao
) : ViewModel() {

    val reminders = mutableStateListOf<Reminder>()

    fun loadReminders(onLoaded: ((List<Reminder>) -> Unit)? = null) {
        viewModelScope.launch {
            val savedReminders = withContext(Dispatchers.IO) {
                reminderDao.getAllReminders().map { it.toReminder() }
            }

            reminders.clear()
            reminders.addAll(savedReminders)
            onLoaded?.invoke(savedReminders)
        }
    }

    fun addReminder(reminder: Reminder) {
        reminders.add(reminder)

        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.insertReminder(reminder.toEntity())
        }
    }

    fun updateReminder(reminder: Reminder) {
        val index = reminders.indexOfFirst { it.id == reminder.id }
        if (index != -1) {
            reminders[index] = reminder
        }

        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.updateReminder(reminder.toEntity())
        }
    }

    fun deleteReminder(reminder: Reminder) {
        reminders.removeAll { it.id == reminder.id }

        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.deleteReminder(reminder.toEntity())
        }
    }

    fun rescheduleEnabledReminders(scheduler: AlarmScheduler) {
        reminders.forEach { reminder ->
            if (reminder.isEnabled) {
                scheduler.scheduleReminderAfterFiveSeconds(reminder)
            }
        }
    }
}