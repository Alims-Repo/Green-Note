package com.alim.greennote.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.format.DateFormat
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.alim.greennote.data.model.DrawingEntity
import com.alim.greennote.data.model.ModelTask
import com.alim.greennote.databinding.ItemDrawingBinding
import com.alim.greennote.databinding.ItemTaskBinding
import com.alim.greennote.di.Injection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdapterNotes : RecyclerView.Adapter<AdapterNotes.ViewHolder>() {

    private val data = ArrayList<Any>()

    @SuppressLint("NotifyDataSetChanged")
    fun update(newData: List<Any>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ViewBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(model: Any) {
            when(model) {
                is ModelTask -> bind(model)
                is DrawingEntity -> bind(model)
            }
        }

        private fun bind(model: DrawingEntity) {
            binding as ItemDrawingBinding
            CoroutineScope(Dispatchers.Main).launch {
//                delay(250L)
                binding.drawing.setImageBitmap(
                    base64ToBitmap(model.drawingData)
                )

                binding.date.text = DateFormat.format("hh:mm a, dd MMM yyyy", model.createdAt).toString()
            }
        }

        fun base64ToBitmap(base64String: String?): Bitmap {
            val decodedBytes: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }

        private fun bind(model: ModelTask) {
            binding as ItemTaskBinding
            binding.taskTitle.text = model.title

            val dueDate = DateFormat.format("hh:mm a, dd MMM yyyy", model.dueDateMillis)
            binding.taskDueDate.text = dueDate

            binding.taskCategory.text = model.category

            binding.colorIndicator.setBackgroundColor(model.color)

            binding.checkboxCompleted.isChecked = model.completed

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
    ) = ViewHolder(
        when(Type.entries.get(type)) {
            Type.TYPE_TASK -> {
                ItemTaskBinding.inflate(
                    LayoutInflater.from(view.context), view, false
                )
            }
            Type.TYPE_DRAWING -> {
                ItemDrawingBinding.inflate(
                    LayoutInflater.from(view.context), view, false
                )
            }
        }
    )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(data[position])
    }

    override fun getItemViewType(position: Int): Int {
        return when(data[position]) {
            is ModelTask -> Type.TYPE_TASK.ordinal
            is DrawingEntity -> Type.TYPE_DRAWING.ordinal
            else -> throw IllegalArgumentException("Unknown type")
        }
    }

    override fun getItemCount() = data.size

    enum class Type {
        TYPE_TASK, TYPE_DRAWING
    }
}