package com.beakash.bereminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val message: String,
    val intervalHours: Int,
    val isEnabled: Boolean,
    val repeatEndMode: String,
    val maxOccurrences: Int?,
    val untilDateTimeMillis: Long?,
    val currentOccurrences: Int
)