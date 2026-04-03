package com.beakash.bereminder.ui

import com.beakash.bereminder.model.RepeatEndMode

data class ReminderFormState(
    val title: String = "",
    val message: String = "",
    val intervalText: String = "",
    val repeatEndMode: RepeatEndMode = RepeatEndMode.NEVER,
    val maxOccurrencesText: String = "",
    val untilDateTimeMillis: Long? = null,
    val errorMessage: String? = null
)