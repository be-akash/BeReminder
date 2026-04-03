package com.beakash.bereminder

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beakash.bereminder.alarm.AlarmScheduler
import com.beakash.bereminder.model.Reminder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        onRequestExactAlarmAccess = {
                            openExactAlarmSettings()
                        },
                        onScheduleClick = {
                            val reminder = Reminder(
                                id = 1,
                                title = "Medicine Reminder",
                                message = "Time to take medicine",
                                intervalHours = 1
                            )

                            AlarmScheduler(this).scheduleReminderAfterFiveSeconds(reminder)
                        },
                        exactAlarmAllowed = AlarmScheduler(this).canScheduleExactAlarms()
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
fun MainScreen(
    onRequestExactAlarmAccess: () -> Unit,
    onScheduleClick: () -> Unit,
    exactAlarmAllowed: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (exactAlarmAllowed) {
                "Exact alarm access: Allowed"
            } else {
                "Exact alarm access: Not allowed"
            }
        )

        Button(
            onClick = onRequestExactAlarmAccess,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Open Exact Alarm Settings")
        }

        Button(
            onClick = onScheduleClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Schedule in 5 Seconds")
        }
    }
}