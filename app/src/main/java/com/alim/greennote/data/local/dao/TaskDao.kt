package com.alim.greennote.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.alim.greennote.data.model.ModelTask

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: ModelTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<ModelTask>)

    @Update
    suspend fun updateTask(task: ModelTask)

    @Delete
    fun deleteTask(task: ModelTask)

    @Query("SELECT * FROM tasks ORDER BY dueDateMillis ASC")
    fun getAllTasks(): List<ModelTask>

    @Query("SELECT * FROM tasks ORDER BY dueDateMillis ASC")
    fun getAllTasksLive(): LiveData<List<ModelTask>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): ModelTask?

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}