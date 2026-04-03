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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.beakash.bereminder.alarm.AlarmScheduler
import com.beakash.bereminder.data.ReminderDatabase
import com.beakash.bereminder.data.toEntity
import com.beakash.bereminder.data.toReminder
import com.beakash.bereminder.model.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        val database = ReminderDatabase.getDatabase(this)
        val reminderDao = database.reminderDao()

        setContent {
            val reminders = remember { mutableStateListOf<Reminder>() }

            LaunchedEffect(Unit) {
                val savedReminders = withContext(Dispatchers.IO) {
                    reminderDao.getAllReminders().map { it.toReminder() }
                }

                reminders.clear()
                reminders.addAll(savedReminders)

                val scheduler = AlarmScheduler(this@MainActivity)

                savedReminders.forEach { reminder ->
                    if (reminder.isEnabled) {
                        scheduler.scheduleReminderAfterFiveSeconds(reminder)
                    }
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReminderFormScreen(
                        exactAlarmAllowed = AlarmScheduler(this).canScheduleExactAlarms(),
                        reminders = reminders,
                        onRequestExactAlarmAccess = {
                            openExactAlarmSettings()
                        },
                        onScheduleReminder = { reminder ->
                            CoroutineScope(Dispatchers.IO).launch {
                                reminderDao.insertReminder(reminder.toEntity())
                            }

                            reminders.add(reminder)
                            AlarmScheduler(this).scheduleReminderAfterFiveSeconds(reminder)

                            Toast.makeText(
                                this,
                                "Reminder saved and scheduled",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onToggleReminder = { updatedReminder ->
                            val index = reminders.indexOfFirst { it.id == updatedReminder.id }
                            if (index != -1) {
                                reminders[index] = updatedReminder
                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                reminderDao.updateReminder(updatedReminder.toEntity())
                            }

                            val scheduler = AlarmScheduler(this)
                            if (!updatedReminder.isEnabled) {
                                scheduler.cancelReminder(updatedReminder.id)
                            } else {
                                scheduler.scheduleReminderAfterFiveSeconds(updatedReminder)
                            }
                        },
                        onDeleteReminder = { reminderToDelete ->
                            reminders.removeAll { it.id == reminderToDelete.id }

                            CoroutineScope(Dispatchers.IO).launch {
                                reminderDao.deleteReminder(reminderToDelete.toEntity())
                            }

                            AlarmScheduler(this).cancelReminder(reminderToDelete.id)
                        }
                    )
                }
            }
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
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
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var intervalText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = if (exactAlarmAllowed) {
                    "Exact alarm access: Allowed"
                } else {
                    "Exact alarm access: Not allowed"
                },
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            Button(
                onClick = onRequestExactAlarmAccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Open Exact Alarm Settings")
            }
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                label = { Text("Reminder Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }

        item {
            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                    errorMessage = null
                },
                label = { Text("Reminder Message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        item {
            OutlinedTextField(
                value = intervalText,
                onValueChange = {
                    intervalText = it
                    errorMessage = null
                },
                label = { Text("Interval Hours") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        item {
            Button(
                onClick = {
                    val intervalHours = intervalText.toIntOrNull()

                    when {
                        title.isBlank() -> {
                            errorMessage = "Title cannot be empty"
                        }
                        message.isBlank() -> {
                            errorMessage = "Message cannot be empty"
                        }
                        intervalHours == null -> {
                            errorMessage = "Interval must be a number"
                        }
                        intervalHours <= 0 -> {
                            errorMessage = "Interval must be greater than 0"
                        }
                        else -> {
                            errorMessage = null

                            val reminder = Reminder(
                                id = System.currentTimeMillis().toInt(),
                                title = title.trim(),
                                message = message.trim(),
                                intervalHours = intervalHours,
                                isEnabled = true
                            )

                            onScheduleReminder(reminder)

                            title = ""
                            message = ""
                            intervalText = ""
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Create Reminder")
            }
        }

        item {
            Text(
                text = "Created Reminders",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }

        items(reminders) { reminder ->
            ReminderItem(
                reminder = reminder,
                onToggleEnabled = { updatedReminder ->
                    onToggleReminder(updatedReminder)
                },
                onDelete = { reminderToDelete ->
                    onDeleteReminder(reminderToDelete)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggleEnabled: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = { onDelete(reminder) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete reminder"
                    )
                }
            }

            Text(
                text = reminder.message,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Every ${reminder.intervalHours} hour(s)",
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (reminder.isEnabled) "Enabled" else "Disabled"
                )

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { isChecked ->
                        onToggleEnabled(reminder.copy(isEnabled = isChecked))
                    }
                )
            }
        }
    }
}