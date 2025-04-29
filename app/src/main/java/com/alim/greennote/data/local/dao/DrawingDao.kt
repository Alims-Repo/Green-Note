package com.alim.greennote.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alim.greennote.data.model.DrawingEntity

@Dao
interface DrawingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: DrawingEntity): Long

    @Update
    suspend fun updateDrawing(drawing: DrawingEntity)

    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawingById(id: Long): DrawingEntity?

    @Query("SELECT * FROM drawings ORDER By createdAt DESC")
    fun getDrawings(): List<DrawingEntity>

    @Query("SELECT * FROM drawings")
    fun getDrawingsLive(): LiveData<List<DrawingEntity>>

    @Query("SELECT * FROM drawings WHERE id = :taskId")
    suspend fun getDrawingByTaskId(taskId: Long): DrawingEntity?

    @Query("DELETE FROM drawings WHERE id = 1")
    suspend fun delete()
}