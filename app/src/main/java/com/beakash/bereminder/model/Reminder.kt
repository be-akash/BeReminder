package com.beakash.bereminder.model

data class Reminder(
    val id: Int,
    val title: String,
    val message: String,
    val intervalHours: Int,
    val isEnabled: Boolean = true,
    val repeatEndMode: RepeatEndMode = RepeatEndMode.NEVER,
    val maxOccurrences: Int? = null,
    val untilDateTimeMillis: Long? = null,
    val currentOccurrences: Int = 0
)