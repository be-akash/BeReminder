# Hourly Reminder Android App

## 📌 Overview

A simple, reliable Android reminder app focused on **hourly repeating reminders** (similar to Samsung Reminder), designed for Pixel and other Android devices.

Primary goal:

> Deliver accurate, user-friendly reminders with sound, vibration, and flexible repeat rules.

---

## 🚀 Core Features (Version 1)

### 1. Create Reminder

* Title
* Optional note
* Start date & time
* Repeat type:

  * One-time
  * Every X hours

### 2. Repeat Options

* Interval in hours (1, 2, 3, 4, 6, 8, 12, 24)
* Custom interval (optional later)

### 3. Repeat Ending Modes

* Never (indefinite)
* After X occurrences
* Until specific date/time

### 4. Reminder Controls

* Enable / Disable toggle
* Edit reminder
* Delete reminder

### 5. Notifications

* Title + message
* Works on lock screen
* Sound + vibration
* High priority (heads-up notification)

### 6. Notification Actions

* Done
* Snooze (5, 10, 15, 30, 60 min)

### 7. Daily Active Window

* Example: only between 8:00 AM – 10:00 PM

### 8. Reboot Restore

* Reminders are restored after device restart

---

## 🧠 Reminder Behavior Rules

### Repeat Logic

* Only one alarm is scheduled at a time
* After firing, next occurrence is calculated and scheduled

### Snooze Behavior

* Snooze does NOT count as a new occurrence
* It only delays the current reminder

### Completion Behavior

* "Done" dismisses current notification
* Next occurrence remains scheduled

### End Conditions

* Stop when:

  * Max occurrences reached
  * End date/time reached

### Active Window Handling

* If next time is outside allowed hours:

  * Move to next valid time window

---

## 📱 App Screens

### 1. Home Screen

* List of reminders
* Shows:

  * Title
  * Next trigger time
  * Repeat info
  * Active/Paused state

### 2. Add/Edit Reminder Screen

Fields:

* Title
* Note
* Start time
* Repeat type
* Interval hours
* End condition
* Active hours
* Sound/Vibration

### 3. Settings / Permissions Screen

* Notification permission status
* Exact alarm permission status
* Battery optimization guidance

---

## 🔔 Notification Features

* High-priority notification
* Sound + vibration
* Lock screen support
* Action buttons (Done, Snooze)

Optional (future):

* Custom sounds
* Full-screen alerts

---

## ⚙️ Technical Architecture

### Language & UI

* Kotlin
* Jetpack Compose

### Storage

* Room Database

### Scheduling

* AlarmManager (exact alarms)

### Components

* BroadcastReceiver (trigger reminders)
* BootReceiver (restore after reboot)

---

## 🔐 Permissions Required

* POST_NOTIFICATIONS (Android 13+)
* SCHEDULE_EXACT_ALARM (Android 14+)
* RECEIVE_BOOT_COMPLETED

---

## ⚠️ Platform Considerations

### Android 13+

* Must request notification permission

### Android 14+

* Exact alarms require special access

### Battery Optimization

* May affect reliability
* Provide guidance screen to user

---

## 🧩 Feature Priority

### Must Have

* Create reminder
* Hourly repeat
* Notifications with sound
* Snooze
* Enable/disable
* Reboot restore

### Should Have

* Notes
* Active hours
* End conditions
* Permission screen

### Later

* Categories
* Widgets
* Cloud sync
* Backup/export

---

## 🛠 Development Phases

### Phase 1: Planning

* Define data model
* Define behavior rules
* Define UI screens

### Phase 2: UI + Data

* Build Compose UI
* Implement Room database

### Phase 3: Scheduling

* Implement AlarmManager
* Handle repeat logic
* Add notifications

### Phase 4: Reliability

* Reboot restore
* Permissions handling
* Testing on Pixel & Samsung

### Phase 5: Polish

* UI improvements
* Add pause, filtering, history

---

## 🎯 Product Direction

* Clean and minimal UI
* Fast reminder creation
* Strong hourly repeat support
* High reliability

---

## ✅ Example Use Cases

* Take medicine every 6 hours (8 times)
* Drink water every 2 hours (indefinite)
* Check temperature every 1 hour until specific date

---

## 📌 Summary

This app focuses on:

* Simplicity
* Reliability
* Powerful hourly reminders

Built specifically to fill the gap on Pixel devices where no native hourly reminder app exists.
