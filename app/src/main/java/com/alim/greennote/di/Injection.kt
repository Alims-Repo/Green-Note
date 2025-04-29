package com.alim.greennote.di

import android.content.Context
import com.alim.greennote.data.dao.TaskDao
import com.alim.greennote.data.local.AppDatabase
import com.alim.greennote.data.local.DatabaseProvider
import com.alim.greennote.data.repository.DrawingRepository
import com.google.gson.Gson

object Injection {

    private var _gson: Gson? = null

    private var _appDatabase: AppDatabase? = null

    private var _drawingRepository: DrawingRepository? = null

    val taskDao get() = _appDatabase!!.taskDao()

    val drawingDao get() = _appDatabase!!.drawingDao()

    val repository get() = _drawingRepository!!

    fun init(context: Context) {
        _gson = Gson()
        _appDatabase = DatabaseProvider.getDatabase(context)
        _drawingRepository = DrawingRepository(_appDatabase!!.drawingDao(), _gson!!)
    }
}