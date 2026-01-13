package com.example.remindme

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
        todoList[position].isCompleted = !todoList[position].isCompleted
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
    }
}