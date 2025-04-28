package com.alim.greennote.di

import android.content.Context
import com.alim.greennote.data.dao.TaskDao
import com.alim.greennote.data.local.AppDatabase
import com.alim.greennote.data.local.DatabaseProvider

object Injection {

    private var _appDatabase: AppDatabase? = null

    val taskDao get() = _appDatabase!!.taskDao()

    fun init(context: Context) {
        _appDatabase = DatabaseProvider.getDatabase(context)
    }
}