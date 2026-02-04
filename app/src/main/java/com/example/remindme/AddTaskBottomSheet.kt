package com.example.remindme

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar
import java.util.zip.Inflater

class AddTaskBottomSheet(
    private val onTaskAdded: (TodoItem) -> Unit) : BottomSheetDialogFragment() {

    private val dayMap = mapOf(
        0 to Calendar.SUNDAY,
        1 to Calendar.MONDAY,
        2 to Calendar.TUESDAY,
        3 to Calendar.WEDNESDAY,
        4 to Calendar.THURSDAY,
        5 to Calendar.FRIDAY,
        6 to Calendar.SATURDAY
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Task details
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        // Reminder
        val switchReminder = view.findViewById<Switch>(R.id.switchReminder)
        val layoutReminder = view.findViewById<LinearLayout>(R.id.layoutReminderOptions)

        val spinnerDay = view.findViewById<Spinner>(R.id.spinnerDay)
        val days = listOf("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday")
        spinnerDay.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, days)

        var selectedHour = 9
        var selectedMinute = 0

        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            layoutReminder.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Click Listeners
        btnAdd.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if(title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }

            val reminderEnabled = switchReminder.isChecked

            val todoItem = if (reminderEnabled) {
                val selectedDayIndex = spinnerDay.selectedItemPosition
                val calendarDay = dayMap[selectedDayIndex]!!

                TodoItem(
                    title = title,
                    description = description,
                    isReminder = true,
                    reminderDay = calendarDay,
                    reminderHour = selectedHour,
                    reminderMinute = selectedMinute
                )
            } else {
                TodoItem(title = title, description = description)
            }

            if (todoItem.isReminder) {
                scheduleReminder(todoItem)
            }

            onTaskAdded(todoItem)
            dismiss()
        }

        view.findViewById<Button>(R.id.btnPickTime).setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
            }, selectedHour, selectedMinute, false).show()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun scheduleReminder(todo: TodoItem) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, todo.reminderDay!!)
            set(Calendar.HOUR_OF_DAY, todo.reminderHour!!)
            set(Calendar.MINUTE, todo.reminderMinute!!)
            set(Calendar.SECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("TASK_TITLE", todo.title)
            putExtra("TASK_ID", todo.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            todo.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
    }
}