package com.alim.greennote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alim.greennote.data.dao.TaskDao
import com.alim.greennote.data.model.ModelTask

@Database(entities = [ModelTask::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
}