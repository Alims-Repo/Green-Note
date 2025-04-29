package com.alim.greennote.data.model

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class ModelNote(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    val title: String = "",
    val description: String = "",
    override val createdAt: Long = System.currentTimeMillis(),
): ModelId(id, createdAt, title + description)