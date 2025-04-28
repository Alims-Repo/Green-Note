package com.alim.greennote.data.model

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class ModelTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val dueDateMillis: Long = 100,
    val priority: String = "",
    val category: String = "",
    val color: Int = Color.GREEN,
    val autoArchiveDays: Int = 0,
    val completed: Boolean = false,
    val hasDrawing: Boolean = false
)