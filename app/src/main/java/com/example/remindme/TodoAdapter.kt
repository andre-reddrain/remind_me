package com.example.remindme

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private val todoList: MutableList<TodoItem>,
    private val onDelete: (Int) -> Unit,
    private val onToggle: (Int) -> Unit,
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todoList[position]
        holder.tvTitle.text = todo.title
        holder.tvDescription.text = todo.description

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = todo.isCompleted

        if (todo.isCompleted) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.checkBox.setOnCheckedChangeListener { _, _ ->
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onToggle(adapterPosition)
            }
        }

        holder.btnDelete.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onDelete(adapterPosition)
            }
        }
    }

    override fun getItemCount() = todoList.size

    class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkBoxTodo)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }
}