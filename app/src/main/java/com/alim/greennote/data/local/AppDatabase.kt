package com.alim.greennote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alim.greennote.data.local.dao.DrawingDao
import com.alim.greennote.data.local.dao.NoteDao
import com.alim.greennote.data.local.dao.TaskDao
import com.alim.greennote.data.model.DrawingEntity
import com.alim.greennote.data.model.ModelNote
import com.alim.greennote.data.model.ModelTask

@Database(entities = [
    ModelTask::class,
    ModelNote::class,
    DrawingEntity::class
], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    abstract fun noteDao(): NoteDao

    abstract fun drawingDao(): DrawingDao
}