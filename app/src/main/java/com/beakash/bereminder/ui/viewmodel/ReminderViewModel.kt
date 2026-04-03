package com.beakash.bereminder.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beakash.bereminder.alarm.AlarmScheduler
import com.beakash.bereminder.data.ReminderRepository
import com.beakash.bereminder.model.Reminder
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository
) : ViewModel() {

    val reminders = mutableStateListOf<Reminder>()

    fun loadReminders(onLoaded: ((List<Reminder>) -> Unit)? = null) {
        viewModelScope.launch {
            val savedReminders = repository.getAllReminders()

            reminders.clear()
            reminders.addAll(savedReminders)
            onLoaded?.invoke(savedReminders)
        }
    }

    fun createReminder(reminder: Reminder) {
        reminders.add(reminder)

        viewModelScope.launch {
            repository.insertReminder(reminder)
        }
    }

    fun toggleReminder(updatedReminder: Reminder) {
        val index = reminders.indexOfFirst { it.id == updatedReminder.id }
        if (index != -1) {
            reminders[index] = updatedReminder
        }

        viewModelScope.launch {
            repository.updateReminder(updatedReminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        reminders.removeAll { it.id == reminder.id }

        viewModelScope.launch {
            repository.deleteReminder(reminder)
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