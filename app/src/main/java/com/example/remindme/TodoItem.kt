package com.example.remindme

data class TodoItem (
    val id: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
    val title: String,
    val description: String,
    var isCompleted: Boolean = false,
    var isReminder: Boolean = false,
    var reminderDay: Int? = null,
    var reminderHour: Int? = null,
    var reminderMinute: Int? = null
)