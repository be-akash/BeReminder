package com.beakash.bereminder.model

data class Reminder(
    val id: Int,
    val title: String,
    val message: String,
    val intervalHours: Int,
    val isEnabled: Boolean = true
)