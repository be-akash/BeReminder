package com.beakash.bereminder.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beakash.bereminder.alarm.AlarmScheduler
import com.beakash.bereminder.data.ReminderRepository
import com.beakash.bereminder.model.Reminder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository
) : ViewModel() {

    val reminders = mutableStateListOf<Reminder>()

    init {
        observeReminders()
    }

    private fun observeReminders() {
        viewModelScope.launch {
            repository.remindersFlow.collectLatest { savedReminders ->
                reminders.clear()
                reminders.addAll(savedReminders)
            }
        }
    }

    fun createReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder)
        }
    }

    fun toggleReminder(updatedReminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(updatedReminder)
        }
    }

    fun updateReminder(updatedReminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(updatedReminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
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