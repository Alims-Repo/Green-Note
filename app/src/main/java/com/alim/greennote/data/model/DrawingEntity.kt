package com.alim.greennote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
data class DrawingEntity(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    val drawingData: String,
    override val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
): ModelId(id, createdAt, "drawing")

open class ModelId(
    open val id: Long,
    open val createdAt: Long,
    open var queryString: String
)