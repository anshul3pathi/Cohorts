package com.example.cohorts.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.cohorts.core.model.Task
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cohorts.databinding.ItemTasksBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class TasksAdapter(
    options: FirebaseRecyclerOptions<Task>,
    private val checkClickListener: (Task) -> Unit
) : FirebaseRecyclerAdapter<Task, ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTasksBinding.inflate(inflater)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Task) {
        (holder as TaskViewHolder).bind(model)
    }

    inner class TaskViewHolder(private val binding: ItemTasksBinding): ViewHolder(binding.root) {

        fun bind(taskItem: Task) {
            binding.apply {
                task = taskItem
                completeCheckbox.setOnClickListener {
                    checkClickListener(taskItem)
                }
            }
        }

    }

}