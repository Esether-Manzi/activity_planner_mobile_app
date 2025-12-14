package com.example.activityplanner.ui.tasks


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.activityplanner.R
import com.example.activityplanner.model.Task
import java.text.SimpleDateFormat
import java.util.*

/**
 * TaskAdapter renders task items with priority badge, edit/delete icons, and a checkbox for multi-select.
 */
class TaskAdapter(
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onComplete: (Task, Boolean) -> Unit // new callback
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(v)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDates: TextView = itemView.findViewById(R.id.tvDates)
        private val tvPriorityBadge: TextView = itemView.findViewById(R.id.tvPriorityBadge)
        private val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)

        fun bind(task: Task) {
            tvTitle.text = task.title
            tvDescription.text = task.description
            tvCategory.text = task.category
            tvDates.text = "Start: ${formatDate(task.startDateTime)} | Deadline: ${formatDate(task.deadlineDateTime)}"

            // Priority badge
            tvPriorityBadge.text = task.priority
            tvPriorityBadge.setBackgroundColor(
                when (task.priority) {
                    "High" -> itemView.context.getColor(R.color.priority_high_red)
                    "Medium" -> itemView.context.getColor(R.color.priority_medium_orange)
                    else -> itemView.context.getColor(R.color.priority_low_light_blue)
                }
            )

            // Show completed state visually
            applyCompletionStyle(task.completed)

            // Checkbox reflects completion
            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = task.completed
            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                applyCompletionStyle(isChecked)
                onComplete(task, isChecked) // notify caller to update DB/state
            }

            btnEdit.setOnClickListener { onEdit(task) }
            btnDelete.setOnClickListener { onDelete(task) }
        }

        private fun applyCompletionStyle(completed: Boolean) {
            if (completed) {
                tvTitle.paintFlags = tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvDescription.paintFlags = tvDescription.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvPriorityBadge.alpha = 0.5f
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvDescription.paintFlags = tvDescription.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvPriorityBadge.alpha = 1f
            }
        }

        private fun formatDate(millis: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
        }
    }
}
