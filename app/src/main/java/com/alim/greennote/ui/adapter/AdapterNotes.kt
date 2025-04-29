package com.alim.greennote.ui.adapter

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.alim.greennote.data.model.ModelTask
import com.alim.greennote.databinding.ItemTaskBinding

class AdapterNotes : RecyclerView.Adapter<AdapterNotes.ViewHolder>() {

    private val data = ArrayList<ModelTask>()

    @SuppressLint("NotifyDataSetChanged")
    fun update(newData: List<ModelTask>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemTaskBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(model: ModelTask) {
            binding.taskTitle.text = model.title

            // Set due date (convert millis to a formatted date string)
            val dueDate = DateFormat.format("hh:mm a, dd MMM yyyy", model.dueDateMillis)
            binding.taskDueDate.text = dueDate

            // Set category (could be a color, or any string-based category)
            binding.taskCategory.text = model.category

            // Set the color indicator (based on the task color)
            binding.colorIndicator.setBackgroundColor(model.color)

            // Set checkbox state (whether task is completed or not)
            // Here, you can set the state based on a property in the model (like `completed`)
            // For now, assuming `completed` is a boolean in `ModelTask` class
            binding.checkboxCompleted.isChecked = model.completed

            // If the task has a drawing, you can show the drawing indicator
            if (model.hasDrawing) {
                binding.drawingIndicator.visibility = View.VISIBLE
            } else {
                binding.drawingIndicator.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(
        view: ViewGroup,
        type: Int
    ): ViewHolder {
        return ViewHolder(
            ItemTaskBinding.inflate(
                LayoutInflater.from(view.context), view, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size
}