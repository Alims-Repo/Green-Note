package com.alim.greennote.utils

import androidx.recyclerview.widget.DiffUtil
import com.alim.greennote.data.model.DrawingEntity
import com.alim.greennote.data.model.ModelNote
import com.alim.greennote.data.model.ModelTask

class NotesDiffCallback(
    private val oldList: List<Any>,
    private val newList: List<Any>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return when {
            oldItem is ModelNote && newItem is ModelNote -> oldItem.id == newItem.id
            oldItem is ModelTask && newItem is ModelTask -> oldItem.id == newItem.id
            oldItem is DrawingEntity && newItem is DrawingEntity -> oldItem.id == newItem.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
}