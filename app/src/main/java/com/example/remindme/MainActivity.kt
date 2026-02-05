package com.example.remindme

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private val PREFS_NAME = "todo_prefs"
    private val KEY_TODOS = "todos"
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TodoAdapter

    private val todoList = mutableListOf<TodoItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createNotificationChannel()
        requestNotificationPermission()

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TodoAdapter(todoList, ::onDeleteTask, ::onToggleTask)
        recyclerView.adapter = adapter
        loadTodos()

        fabAdd.setOnClickListener {
            val bottomSheet = AddTaskBottomSheet {
                task -> addTask(task)
            }
            bottomSheet.show(supportFragmentManager, "AddTaskButtonSheet")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_reminder_channel",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled task reminders"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun scheduleReminder(todo: TodoItem) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ exact alarm permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        // Tests only!
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
        }

        /*
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, todo.reminderDay!!)
            set(Calendar.HOUR_OF_DAY, todo.reminderHour!!)
            set(Calendar.MINUTE, todo.reminderMinute!!)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        */
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("TASK_TITLE", todo.title)
            putExtra("TASK_ID", todo.id)
        }

        val requestCode = (todo.id % Int.MAX_VALUE).toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelReminder(todo: TodoItem) {
        val intent = Intent(this, ReminderReceiver::class.java)
        val requestCode = (todo.id % Int.MAX_VALUE).toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun loadTodos() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = prefs.getString(KEY_TODOS, null)

        if (json != null) {
            val type = object : TypeToken<MutableList<TodoItem>>() {}.type
            val savedTodos: MutableList<TodoItem> = Gson().fromJson(json, type)
            todoList.clear()
            todoList.addAll(savedTodos)
            adapter.notifyDataSetChanged()
        }
    }

    private fun onDeleteTask(position: Int) {
        val todo = todoList[position]

        if (todo.isReminder) {
            cancelReminder(todo)
        }

        todoList.removeAt(position)
        adapter.notifyItemRemoved(position)
        saveTodos()
    }

    private fun saveTodos() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = Gson().toJson(todoList)
        prefs.edit().putString(KEY_TODOS, json).apply()
    }

    private fun onToggleTask(position: Int) {
        val todo = todoList[position]
        todo.isCompleted = !todo.isCompleted

        if (todo.isReminder) {
            if (todo.isCompleted) {
                cancelReminder(todo)
            } else {
                scheduleReminder(todo)
            }
        }
        recyclerView.post {
            adapter.notifyItemChanged(position)
        }
        saveTodos()
    }

    private fun addTask(task: TodoItem) {
        todoList.add(0, task)
        adapter.notifyItemInserted(0)
        recyclerView.smoothScrollToPosition(0)
        saveTodos()

        if (task.isReminder && task.reminderDay != null && task.reminderHour != null && task.reminderMinute != null) {
            scheduleReminder(task)
            Toast.makeText(this, "Reminder scheduled!", Toast.LENGTH_SHORT).show()
        }
    }
}