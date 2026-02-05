package com.example.remindme

import android.app.TimePickerDialog
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
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

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
        val tvSelectedTime = view.findViewById<TextView>(R.id.tvSelectedTime)

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

            onTaskAdded(todoItem)
            dismiss()
        }

        view.findViewById<Button>(R.id.btnPickTime).setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute

                val formattedTime = String.format("%02d:%02d", hour, minute)
                tvSelectedTime.text = "Time: $formattedTime"

            }, selectedHour, selectedMinute, false).show()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}