package com.alim.greennote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alim.greennote.data.dao.DrawingDao
import com.alim.greennote.data.dao.TaskDao
import com.alim.greennote.data.model.DrawingEntity
import com.alim.greennote.data.model.ModelTask

@Database(entities = [ModelTask::class, DrawingEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    abstract fun drawingDao(): DrawingDao
}