package com.alim.greennote.data.local

import android.content.Context
import androidx.room.Room
import com.alim.greennote.data.local.AppDatabase

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "green_note_database"
            ).allowMainThreadQueries().build()
            INSTANCE = instance
            instance
        }
    }
}