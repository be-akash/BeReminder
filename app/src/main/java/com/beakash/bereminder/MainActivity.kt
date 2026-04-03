package com.beakash.bereminder

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beakash.bereminder.alarm.AlarmScheduler
import com.beakash.bereminder.data.ReminderDatabase
import com.beakash.bereminder.data.ReminderRepository
import com.beakash.bereminder.model.Reminder
import com.beakash.bereminder.model.RepeatEndMode
import com.beakash.bereminder.ui.ReminderFormState
import com.beakash.bereminder.ui.viewmodel.ReminderViewModel
import com.beakash.bereminder.ui.viewmodel.ReminderViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        val database = ReminderDatabase.getDatabase(this)
        val repository = ReminderRepository(database.reminderDao())

        setContent {
            val viewModel: ReminderViewModel = viewModel(
                factory = ReminderViewModelFactory(repository)
            )

            val scheduler = AlarmScheduler(this)

            LaunchedEffect(Unit) {
                viewModel.rescheduleEnabledReminders(scheduler)
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReminderFormScreen(
                        exactAlarmAllowed = scheduler.canScheduleExactAlarms(),
                        reminders = viewModel.reminders,
                        onRequestExactAlarmAccess = { openExactAlarmSettings() },
                        onScheduleReminder = {
                            viewModel.createReminder(it)
                            scheduler.scheduleReminderAfterFiveSeconds(it)
                            Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show()
                        },
                        onToggleReminder = {
                            viewModel.toggleReminder(it)
                            if (it.isEnabled) {
                                scheduler.scheduleReminderAfterFiveSeconds(it)
                            } else {
                                scheduler.cancelReminder(it.id)
                            }
                        },
                        onDeleteReminder = {
                            viewModel.deleteReminder(it)
                            scheduler.cancelReminder(it.id)
                        }
                    )
                }
            }
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }
    }
}

@Composable
fun ReminderFormScreen(
    exactAlarmAllowed: Boolean,
    reminders: List<Reminder>,
    onRequestExactAlarmAccess: () -> Unit,
    onScheduleReminder: (Reminder) -> Unit,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit
) {
    var formState by remember { mutableStateOf(ReminderFormState()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {

        item {
            Text(
                text = if (exactAlarmAllowed) "Exact alarm allowed"
                else "Exact alarm NOT allowed"
            )
        }

        item {
            Button(
                onClick = onRequestExactAlarmAccess,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Text("Grant Exact Alarm Permission")
            }
        }

        item {
            OutlinedTextField(
                value = formState.title,
                onValueChange = { formState = formState.copy(title = it, errorMessage = null) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
        }

        item {
            OutlinedTextField(
                value = formState.message,
                onValueChange = { formState = formState.copy(message = it, errorMessage = null) },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = formState.intervalText,
                onValueChange = { formState = formState.copy(intervalText = it, errorMessage = null) },
                label = { Text("Interval (hours)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }

        // Repeat Mode
        item {
            Text("Repeat Ends", modifier = Modifier.padding(top = 20.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = formState.repeatEndMode == RepeatEndMode.NEVER,
                    onClick = {
                        formState = formState.copy(
                            repeatEndMode = RepeatEndMode.NEVER,
                            maxOccurrencesText = "",
                            untilDateTimeMillis = null
                        )
                    }
                )
                Text("Never")
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = formState.repeatEndMode == RepeatEndMode.AFTER_COUNT,
                    onClick = {
                        formState = formState.copy(
                            repeatEndMode = RepeatEndMode.AFTER_COUNT,
                            untilDateTimeMillis = null
                        )
                    }
                )
                Text("After Count")
            }
        }

        if (formState.repeatEndMode == RepeatEndMode.AFTER_COUNT) {
            item {
                OutlinedTextField(
                    value = formState.maxOccurrencesText,
                    onValueChange = {
                        formState = formState.copy(maxOccurrencesText = it)
                    },
                    label = { Text("Max Occurrences") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = formState.repeatEndMode == RepeatEndMode.UNTIL_DATE,
                    onClick = {
                        formState = formState.copy(
                            repeatEndMode = RepeatEndMode.UNTIL_DATE,
                            maxOccurrencesText = ""
                        )
                    }
                )
                Text("Until Date (millis)")
            }
        }

        if (formState.repeatEndMode == RepeatEndMode.UNTIL_DATE) {
            item {
                OutlinedTextField(
                    value = formState.untilDateTimeMillis?.toString() ?: "",
                    onValueChange = {
                        formState = formState.copy(untilDateTimeMillis = it.toLongOrNull())
                    },
                    label = { Text("Millis") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (formState.errorMessage != null) {
            item {
                Text(
                    text = formState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            Button(
                onClick = {
                    val interval = formState.intervalText.toIntOrNull()
                    val max = formState.maxOccurrencesText.toIntOrNull()

                    when {
                        formState.title.isBlank() -> formState = formState.copy(errorMessage = "Title required")
                        formState.message.isBlank() -> formState = formState.copy(errorMessage = "Message required")
                        interval == null || interval <= 0 -> formState = formState.copy(errorMessage = "Invalid interval")

                        formState.repeatEndMode == RepeatEndMode.AFTER_COUNT && (max == null || max <= 0) ->
                            formState = formState.copy(errorMessage = "Invalid max count")

                        formState.repeatEndMode == RepeatEndMode.UNTIL_DATE && formState.untilDateTimeMillis == null ->
                            formState = formState.copy(errorMessage = "Date required")

                        else -> {
                            val reminder = Reminder(
                                id = System.currentTimeMillis().toInt(),
                                title = formState.title,
                                message = formState.message,
                                intervalHours = interval,
                                isEnabled = true,
                                repeatEndMode = formState.repeatEndMode,
                                maxOccurrences = if (formState.repeatEndMode == RepeatEndMode.AFTER_COUNT) max else null,
                                untilDateTimeMillis = if (formState.repeatEndMode == RepeatEndMode.UNTIL_DATE) formState.untilDateTimeMillis else null,
                                currentOccurrences = 0
                            )

                            onScheduleReminder(reminder)
                            formState = ReminderFormState()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Create Reminder")
            }
        }

        item {
            Text("Reminders", modifier = Modifier.padding(top = 24.dp))
        }

        items(reminders) { reminder ->
            ReminderItem(reminder, onToggleReminder, onDeleteReminder)
        }

        item { Spacer(modifier = Modifier.height(50.dp)) }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggleEnabled: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    val endText = when (reminder.repeatEndMode) {
        RepeatEndMode.NEVER -> "Ends: Never"

        RepeatEndMode.AFTER_COUNT -> {
            if (reminder.maxOccurrences != null)
                "Ends after ${reminder.maxOccurrences} times"
            else "Ends after -"
        }

        RepeatEndMode.UNTIL_DATE -> {
            if (reminder.untilDateTimeMillis != null)
                "Ends at ${reminder.untilDateTimeMillis}"
            else "Ends at -"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(reminder.title)

                IconButton(onClick = { onDelete(reminder) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            Text(reminder.message)
            Text("Every ${reminder.intervalHours} hour(s)")
            Text(endText)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (reminder.isEnabled) "Enabled" else "Disabled")

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = {
                        onToggleEnabled(reminder.copy(isEnabled = it))
                    }
                )
            }
        }
    }
}