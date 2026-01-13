package com.example.remindme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.zip.Inflater

class AddTaskBottomSheet(
    private val onTaskAdded: (TodoItem) -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        btnAdd.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if(title.isNotEmpty()) {
                val todoItem = TodoItem(title = title, description = description)
                onTaskAdded(todoItem)
                dismiss()
            } else {
                etTitle.error = "Title is required"
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}