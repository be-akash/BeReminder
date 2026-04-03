package com.beakash.bereminder.ui

data class ReminderFormState(
    val title: String = "",
    val message: String = "",
    val intervalText: String = "",
    val errorMessage: String? = null
)