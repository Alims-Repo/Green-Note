package com.alim.greennote.data.repository

import com.alim.greennote.data.local.dao.DrawingDao
import com.alim.greennote.data.model.DrawingEntity
import com.alim.greennote.data.model.SerializableDrawingAction
import com.alim.greennote.data.model.SerializableDrawingAction.Companion.toDrawingAction
import com.alim.greennote.data.model.SerializableDrawingAction.Companion.toSerializable
import com.alim.greennote.ui.views.DrawingView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DrawingRepository(
    private val drawingDao: DrawingDao,
    private val gson: Gson
) {
    suspend fun saveDrawing(taskId: Long, drawingActions: List<DrawingView.DrawingAction>, isTemp: Boolean = false): Long {
        return withContext(Dispatchers.IO) {
            // Convert drawing actions to serializable format
            val serializableActions = drawingActions.map { it.toSerializable() }

            // Convert to JSON
            val drawingJson = gson.toJson(serializableActions)

            // Create entity
            val drawing = DrawingEntity(
                drawingData = drawingJson,
                updatedAt = System.currentTimeMillis()
            )

            // Save to database
            drawingDao.insertDrawing(drawing)
        }
    }

    suspend fun updateDrawing(id: Long, taskId: Long, drawingActions: List<DrawingView.DrawingAction>, isTemp: Boolean = false) {
        withContext(Dispatchers.IO) {
            // Convert drawing actions to serializable format
            val serializableActions = drawingActions.map { it.toSerializable() }

            // Convert to JSON
            val drawingJson = gson.toJson(serializableActions)

            // Create entity
            val drawing = DrawingEntity(
                id = id,
                drawingData = drawingJson,
                updatedAt = System.currentTimeMillis()
            )

            // Update in database
            drawingDao.updateDrawing(drawing)
        }
    }

    suspend fun getDrawingByTaskId(taskId: Long): List<DrawingView.DrawingAction>? {
        return withContext(Dispatchers.IO) {
            val entity = drawingDao.getDrawingByTaskId(taskId) ?: return@withContext null
            parseDrawingJson(entity.drawingData)
        }
    }

    suspend fun getAllDrawing(): List<List<DrawingView.DrawingAction>> {
        return withContext(Dispatchers.IO) {
            val entity = drawingDao.getDrawings()
            entity.map {
                parseDrawingJson(it.drawingData) ?: emptyList()
            }
        }
    }


    private fun parseDrawingJson(json: String): List<DrawingView.DrawingAction>? {
        return try {
            val type = object : TypeToken<List<SerializableDrawingAction>>() {}.type
            val serializableActions: List<SerializableDrawingAction> = gson.fromJson(json, type)
            serializableActions.map { it.toDrawingAction() }
        } catch (e: Exception) {
            null
        }
    }
}