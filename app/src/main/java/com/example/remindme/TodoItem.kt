package com.example.remindme

data class TodoItem (
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)