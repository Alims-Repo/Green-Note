package com.alim.greennote.ui.adapter

import androidx.recyclerview.widget.DiffUtil

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
import com.alim.greennote.data.model.ModelNote
import com.alim.greennote.data.model.ModelTask
import com.alim.greennote.databinding.ItemDrawingBinding
import com.alim.greennote.databinding.ItemNoteBinding
import com.alim.greennote.databinding.ItemTaskBinding
import com.alim.greennote.di.Injection
import com.alim.greennote.utils.NotesDiffCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdapterNotes : RecyclerView.Adapter<AdapterNotes.ViewHolder>() {

    val data = mutableListOf<Any>()

    var onDeleteItem: ((Any) -> Unit)? = null

    fun update(newData: List<Any>) {
        val diffCallback = NotesDiffCallback(data, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        data.clear()
        data.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(
        private val binding: ViewBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(model: Any) {
            when(model) {
                is ModelTask -> bind(model)
                is DrawingEntity -> bind(model)
                is ModelNote -> bind(model)
            }
        }

        private fun bind(model: ModelNote) {
            binding as ItemNoteBinding
            binding.title.text = model.title
            binding.description.text = model.description

            binding.date.text = DateFormat.format("hh:mm a, dd MMM yyyy", model.createdAt).toString()
        }

        private fun bind(model: DrawingEntity) {
            binding as ItemDrawingBinding
            binding.drawing.setImageBitmap(
                base64ToBitmap(model.drawingData)
            )

            binding.date.text = DateFormat.format("hh:mm a, dd MMM yyyy", model.createdAt).toString()
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

            binding.checkboxCompleted.setOnCheckedChangeListener { b, c->
                Injection.taskDao.insertTask(
                    model.copy(completed = c)
                )
            }

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
            Type.TYPE_TASK -> ItemTaskBinding.inflate(
                LayoutInflater.from(view.context), view, false
            )
            Type.TYPE_DRAWING -> ItemDrawingBinding.inflate(
                LayoutInflater.from(view.context), view, false
            )
            Type.TYPE_NOTE -> ItemNoteBinding.inflate(
                LayoutInflater.from(view.context), view, false
            )
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
            is ModelNote -> Type.TYPE_NOTE.ordinal
            else -> throw IllegalArgumentException("Unknown type")
        }
    }

    override fun getItemCount() = data.size

    enum class Type {
        TYPE_TASK, TYPE_DRAWING, TYPE_NOTE
    }
}